package siberia.utils.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import io.ktor.util.date.*
import kotlinx.serialization.json.Json
import siberia.conf.AppConf
import siberia.exceptions.ForbiddenException
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.dto.RefreshTokenDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.user.data.dao.UserDao
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.auth.data.dto.QrTokenDto
import siberia.plugins.Logger
import siberia.utils.database.idValue
import java.util.*

object JwtUtil {
    fun createToken(userDao: UserDao, lastLogin: Long? = null): String {
        return JWT.create()
            .withIssuer(AppConf.jwt.domain)
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(
                Date(
                System.currentTimeMillis() +
                        (if (lastLogin != null) AppConf.jwt.refreshExpirationTime else AppConf.jwt.expirationTime) * 1000
                )
            )
            .apply {
                withClaim("id", userDao.idValue)
                val rules = RbacModel.userToRuleLinks(
                    userDao.idValue, expanded = true
                )
                if (lastLogin != null) {
                    withClaim("lastLogin", lastLogin)
                } else {
                    withClaim("rules", rules.map { Json.encodeToString(LinkedRuleOutputDto.serializer(), it) }.toString())
                }

            }.sign(Algorithm.HMAC256(AppConf.jwt.secret))
    }

    fun createMobileAccessToken(qrTokenDto: QrTokenDto): String {
        return JWT.create()
            .withIssuer(AppConf.jwt.domain)
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(
                Date(System.currentTimeMillis() + AppConf.jwt.mobileExpirationTime * 1000)
            )
            .apply {
                withClaim("id", qrTokenDto.userId)
                if (qrTokenDto.transactionId != null)
                    withClaim("transactionId", qrTokenDto.transactionId)
                if (qrTokenDto.stockId != null)
                    withClaim("stockId", qrTokenDto.stockId)
                val rules = RbacModel.userToRuleLinks(
                    qrTokenDto.userId, expanded = true
                ).toMutableList().apply { add(LinkedRuleOutputDto(ruleId = AppConf.rules.mobileAccess)) }
                withClaim("rules", rules.map { Json.encodeToString(LinkedRuleOutputDto.serializer(), it) }.toString())
            }.sign(Algorithm.HMAC256(AppConf.jwt.secret))
    }

    fun decodeAccessToken(principal: JWTPrincipal): AuthorizedUser = AuthorizedUser(
        id = principal.getClaim("id", Int::class)!!,
        stockId = principal.getClaim("stockId", Int::class),
        transactionId = principal.getClaim("transactionId", Int::class),
        rules = Json.decodeFromString<List<LinkedRuleOutputDto>>(principal.getClaim("rules", String::class) ?: "[]")
    )

    fun decodeRefreshToken(principal: JWTPrincipal): RefreshTokenDto = RefreshTokenDto(
        id = principal.getClaim("id", Int::class)!!,
        lastLogin = principal.getClaim("lastLogin", Long::class)!!
    )

    fun verifyNative(token: String): AuthorizedUser {
        Logger.debug("Verify native", "main")
        val jwtVerifier = JWT
            .require(Algorithm.HMAC256(AppConf.jwt.secret))
            .withIssuer(AppConf.jwt.domain)
            .build()

        val verified = jwtVerifier.verify(token)
        return if (verified != null) {
            val claims = verified.claims
            val currentTime: Long = getTimeMillis() / 1000
            Logger.debug(currentTime, "main")
            Logger.debug(claims["exp"], "main")
            Logger.debug(claims["iss"], "main")
            Logger.debug(claims["id"], "main")
            Logger.debug(claims["rules"], "main")
            if (currentTime > (claims["exp"]?.asInt()
                    ?: 0) || claims["iss"]?.asString() != AppConf.jwt.domain
            ) {
                Logger.debug("expired exception", "main")
                throw ForbiddenException()
            }
            else {
                AuthorizedUser(
                    id = claims["id"]?.asInt() ?: throw ForbiddenException(),
                    rules = Json.decodeFromString<List<LinkedRuleOutputDto>>(claims["rules"]?.asString() ?: "[]")
                )
            }
        } else {
            Logger.debug("verified exception", "main")
            throw ForbiddenException()
        }
    }

}