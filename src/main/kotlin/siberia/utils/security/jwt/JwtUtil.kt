package siberia.utils.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.util.date.*
import kotlinx.serialization.json.Json
import siberia.conf.AppConf
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.idValue
import java.util.*

object JwtUtil {
    fun createToken(userDao: UserDao, refreshToken: Boolean = false): String {
        return JWT.create()
            .withIssuer(AppConf.jwt.domain)
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(
                Date(
                System.currentTimeMillis() +
                        if (refreshToken) AppConf.jwt.refreshExpirationTime else AppConf.jwt.expirationTime
            )
            )
            .apply {
                withClaim("id", userDao.idValue)
                println(userDao.rules.map { Json.encodeToString(LinkedRuleOutputDto.serializer(), it) }.toString())
                if (refreshToken) {
                    withClaim("lastLogin", userDao.lastLogin)
                } else {
                    withClaim("rules", userDao.rules.map { Json.encodeToString(LinkedRuleOutputDto.serializer(), it) }.toString())
                }

            }.sign(Algorithm.HMAC256(AppConf.jwt.secret))
    }

    fun verifyNative(token: String) {
        val jwtVerifier = JWT
            .require(Algorithm.HMAC256(AppConf.jwt.secret))
            .withIssuer(AppConf.jwt.domain)
            .build()

        val verified = jwtVerifier.verify(token)
        if (verified != null) {
            val claims = verified.claims
            val currentTime: Long = getTimeMillis() / 1000
            if (currentTime > (claims["exp"]?.asInt()
                    ?: 0) || claims["iss"]?.asString() != AppConf.jwt.domain
            )
                //Bad token
                println("unauthorized")
            else
                //Nice
                println("authorized")

        } else {
            //Bad sign
            println("unauthorized")
        }
    }

}