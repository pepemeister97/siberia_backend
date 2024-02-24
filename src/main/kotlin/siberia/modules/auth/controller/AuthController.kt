package siberia.modules.auth.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthInputDto
import siberia.modules.auth.service.AuthQrService
import siberia.modules.auth.service.AuthService
import siberia.utils.kodein.KodeinController

class AuthController(override val di: DI) : KodeinController() {
    private val authService: AuthService by instance()
    private val authQrService: AuthQrService by instance()

    override fun Routing.registerRoutes() {
        route("auth") {
            post {
                val authInput = call.receive<AuthInputDto>()

                call.respond(authService.auth(authInput))
            }
            authenticate ("default") {
                route("qr") {
                    post("stock/{stockId}") {
                        val authorizedUser = call.getAuthorized()
                        val stockId = call.parameters.getInt("stockId", "Stock id must be INT")

                        call.respondBytes(authQrService.createStockQr(authorizedUser, stockId))
                    }
                    post("transaction/{transactionId}") {
                        val authorizedUser = call.getAuthorized()
                        val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")

                        call.respondBytes(authQrService.createTransactionQr(authorizedUser, transactionId))
                    }
                    authenticate ("mobile-access") {
                        get {
                            call.respond(call.getAuthorized())
                        }
                    }
                }
            }
            authenticate("mobile-auth") {
                post("mobile") {
                    val authorizedUser = call.getAuthorized()

                    call.respond(authQrService.authorizeMobileApp(authorizedUser))
                }
            }
            authenticate("refresh") {
                post("refresh") {
                    val refreshTokenDto = getRefresh(call)

                    call.respond(authService.refreshUser(refreshTokenDto))
                }
            }
            authenticate("default") {
                route("authorized") {
                    get {
                        call.respond(authService.getAuthorized(call.getAuthorized()))
                    }
                }
            }
        }
    }

}