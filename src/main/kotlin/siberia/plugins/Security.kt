package siberia.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import siberia.conf.AppConf.jwt
import siberia.conf.AppConf.rules
import siberia.utils.security.jwt.JwtUtil

val jwtVerifier: JWTVerifier = JWT
    .require(Algorithm.HMAC256(jwt.secret))
    .withIssuer(jwt.domain)
    .build()

fun AuthenticationConfig.loadRule(ruleName: String, ruleValue: Int) {
    jwt(ruleName) {
        verifier(jwtVerifier)
        validate { jwtCredential ->
            val principal = JWTPrincipal(jwtCredential.payload)

            val authorizedUser = JwtUtil.decodeAccessToken(principal)

            if (authorizedUser.rules.any { it.ruleId == ruleValue})
                principal
            else
                null
        }
    }
}

fun Application.configureSecurity() {
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

        loadRule("user-managing", rules.userManaging)
        loadRule("rbac-managing", rules.rbacManaging)
        loadRule("check-logs", rules.checkLogs)
        loadRule("brand-managing", rules.brandManaging)
        loadRule("collection-managing", rules.collectionManaging)
        loadRule("category-managing", rules.categoryManaging)
        loadRule("products-managing", rules.productsManaging)
        loadRule("stock-managing", rules.stockManaging)
        loadRule("view-products-list", rules.viewProductsList)
        loadRule("concrete-stock-view", rules.concreteStockView)

        loadRule("create-income-request", rules.createIncomeRequest)
        loadRule("approve-income-request", rules.approveIncomeRequest)
        loadRule("create-outcome-request", rules.createOutcomeRequest)
        loadRule("approve-outcome-request", rules.approveOutcomeRequest)
        loadRule("create-transfer-request", rules.createTransferRequest)
        loadRule("solve-not-delivered-problem", rules.solveNotDeliveredProblem)

        loadRule("mobile-access", rules.mobileAccess)
        loadRule("mobile-auth", rules.mobileAuth)
    }
}
