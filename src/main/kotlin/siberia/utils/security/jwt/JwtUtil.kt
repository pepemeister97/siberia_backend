package siberia.utils.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import io.ktor.util.date.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import siberia.conf.AppConf
import siberia.exceptions.ForbiddenException
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.dto.RefreshTokenDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.auth.data.dto.QrTokenDto
import siberia.plugins.Logger
import java.util.*

typealias EncodedRules = MutableMap<Int, MutableList<Int>>

object JwtUtil {
    private fun encodeRules(linkedRules: List<LinkedRuleOutputDto>): EncodedRules {
        val encoded = mutableMapOf<Int, MutableList<Int>>()
        linkedRules.forEach {
            if (it.needStock == true) {
                if (encoded.containsKey(it.ruleId))
                    encoded[it.ruleId]!!.add(it.stockId ?: 0)
                else
                    encoded[it.ruleId] = mutableListOf(it.stockId ?: 0)
            } else {
                encoded[it.ruleId] = mutableListOf()
            }
        }

        return encoded
    }

    private fun decodeRules(encoded: EncodedRules): List<LinkedRuleOutputDto> {
        val decoded = mutableListOf<LinkedRuleOutputDto>()
        encoded.forEach { (key, value) ->
            if (value.isEmpty())
                decoded.add(LinkedRuleOutputDto(ruleId = key))
            else
                value.forEach { decoded.add(LinkedRuleOutputDto(ruleId = key, stockId = it, needStock = true)) }
        }

        return decoded
    }

    fun createToken(userId: Int, lastLogin: Long? = null): String {
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
                withClaim("id", userId)
                val rules = RbacModel.userToRuleLinks(
                    userId, expanded = true
                )
                if (lastLogin != null) {
                    withClaim("lastLogin", lastLogin)
                } else {
                    withClaim("rules", Json.encodeToString(encodeRules(rules)))
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
                withClaim("rules", Json.encodeToString(encodeRules(rules)))
            }.sign(Algorithm.HMAC256(AppConf.jwt.secret))
    }

    fun createMobileAuthToken(qrTokenDto: QrTokenDto): String {
        return JWT.create()
            .withIssuer(AppConf.jwt.domain)
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(
                Date(System.currentTimeMillis() + AppConf.jwt.mobileAuthExpirationTime * 1000)
            )
            .apply {
                withClaim("id", qrTokenDto.userId)
                if (qrTokenDto.transactionId != null)
                    withClaim("transactionId", qrTokenDto.transactionId)
                if (qrTokenDto.stockId != null)
                    withClaim("stockId", qrTokenDto.stockId)
                val rules = listOf(LinkedRuleOutputDto(ruleId = AppConf.rules.mobileAuth))
                withClaim("rules", Json.encodeToString(encodeRules(rules)))
            }.sign(Algorithm.HMAC256(AppConf.jwt.secret))
    }

    fun decodeAccessToken(principal: JWTPrincipal): AuthorizedUser = AuthorizedUser(
        id = principal.getClaim("id", Int::class)!!,
        stockId = principal.getClaim("stockId", Int::class),
        transactionId = principal.getClaim("transactionId", Int::class),
        rules = decodeRules(Json.decodeFromString(principal.getClaim("rules", String::class) ?: "{}"))
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
                    rules = decodeRules(Json.decodeFromString(claims["rules"]?.asString() ?: "{}"))
                )
            }
        } else {
            Logger.debug("verified exception", "main")
            throw ForbiddenException()
        }
    }

}