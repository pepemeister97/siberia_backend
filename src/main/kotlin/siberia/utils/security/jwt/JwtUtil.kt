package siberia.utils.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.util.date.*
import siberia.conf.AppConf
import java.util.*

object JwtUtil {
    fun createToken(claims: MutableMap<String, String>, refreshToken: Boolean = false): String {
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
                claims.forEach {
                    withClaim(it.key, it.value)
                }
                if (refreshToken)
                    withClaim("refreshToken", "use")
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