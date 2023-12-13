package siberia.modules.auth.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.authorization.AuthInputDto
import siberia.modules.auth.service.AuthService
import siberia.utils.kodein.KodeinController

class AuthController(override val di: DI) : KodeinController() {
    private val authService: AuthService by instance()
    override fun Routing.registerRoutes() {
        route("auth") {
            authenticate("refresh") {
                post("refresh") {
                    val refreshTokenDto = getRefresh(call)

                    call.respond(authService.refreshUser(refreshTokenDto))
                }
            }
            post {
                val authInput = call.receive<AuthInputDto>()

                call.respond(authService.auth(authInput))
            }
        }
        authenticate("default") {
            route("authorized") {
                get {
                    call.respond(call.getAuthorized())
                }
            }
        }
    }

}