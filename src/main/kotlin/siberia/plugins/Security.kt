package siberia.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import siberia.conf.AppConf.jwt
import siberia.conf.AppConf.rules
import siberia.utils.security.jwt.JwtUtil

fun Application.configureSecurity() {

    val jwtVerifier = JWT
        .require(Algorithm.HMAC256(jwt.secret))
        .withIssuer(jwt.domain)
        .build()

    authentication {
        jwt("default") {
            verifier(jwtVerifier)
            validate {
                JWTPrincipal(it.payload)
            }
        }

        jwt("refresh") {
            verifier(jwtVerifier)
            validate {
                val refresh = it.payload.claims["lastLogin"]

                if (refresh == null)
                    null
                else
                    JWTPrincipal(it.payload)
            }
        }

        jwt("user-rules-editing") {
            verifier(jwtVerifier)
            validate { jwtCredential ->
                val principal = JWTPrincipal(jwtCredential.payload)

                val authorizedUser = JwtUtil.decodeAccessToken(principal)

                if (authorizedUser.rules.any { it.ruleId == rules.userRulesEditing })
                    principal
                else
                    null
            }
        }
    }
}
