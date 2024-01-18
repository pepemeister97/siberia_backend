package siberia.modules.auth.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthInputDto
import siberia.modules.auth.service.AuthService
import siberia.modules.user.service.UserService
import siberia.utils.kodein.KodeinController

class AuthController(override val di: DI) : KodeinController() {
    private val authService: AuthService by instance()
    private val userService: UserService by instance()
    override fun Routing.registerRoutes() {
        route("auth") {
            post {
                val authInput = call.receive<AuthInputDto>()

                call.respond(authService.auth(authInput))
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
                        val userDto = userService.getOne(call.getAuthorized().id)
                        userDto.rules = call.getAuthorized().rules
                        call.respond(userDto)
                    }
                }
            }
        }
    }

}