package siberia.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.json.Json
import siberia.conf.AppConf.jwt
import siberia.conf.AppConf.rules
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.user.data.dto.AuthorizedUser

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

                val authorizedUser = getFromPrincipal(principal)

                if (authorizedUser.rules.any { it.ruleId == rules.userRulesEditing })
                    principal
                else
                    null
            }
        }
    }
}

fun getFromPrincipal(principal: JWTPrincipal): AuthorizedUser = AuthorizedUser(
    id = principal.getClaim("id", Int::class)!!,
    rules = Json.decodeFromString<List<LinkedRuleOutputDto>>(principal.getClaim("rules", String::class) ?: "[]")
)
