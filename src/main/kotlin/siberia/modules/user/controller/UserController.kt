package siberia.modules.user.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.LinkedRuleInputDto
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.user.service.UserService
import siberia.utils.kodein.KodeinController

class UserController(override val di: DI) : KodeinController() {
    private val userService: UserService by instance()
    override fun Routing.registerRoutes() {
        route("user") {
            authenticate("user-rules-editing") {
                post("rules") {
                    val newRules = call.receive<List<LinkedRuleInputDto>>()
                    val authorizedUser = getAuthorized(call)
                    call.respond<List<LinkedRuleOutputDto>>(userService.addRules(authorizedUser, newRules))
                }
                post("roles") {
                    val newRules = call.receive<List<Int>>()
                    val authorizedUser = getAuthorized(call)
                    call.respond<List<LinkedRuleOutputDto>>(userService.addRoles(authorizedUser, newRules))
                }
            }
        }
    }
}