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
import siberia.modules.auth.data.dto.RoleOutputDto
import siberia.modules.user.service.UserAccessControlService
import siberia.utils.kodein.KodeinController

class UserController(override val di: DI) : KodeinController() {
    private val userAccessControlService: UserAccessControlService by instance()
    override fun Routing.registerRoutes() {
        route("user") {
            get("roles") {
                val authorizedUser = call.getAuthorized()
                call.respond<List<RoleOutputDto>>(userAccessControlService.getUserRoles(authorizedUser))
            }
            get("rules") {
                val authorizedUser = call.getAuthorized()
                call.respond<List<LinkedRuleOutputDto>>(userAccessControlService.getUserRules(authorizedUser))
            }
            authenticate("user-rules-editing") {
                route("rules") {
                    post {
                        val onAppend = call.receive<List<LinkedRuleInputDto>>()
                        val authorizedUser = call.getAuthorized()
                        call.respond<List<LinkedRuleOutputDto>>(userAccessControlService.addRules(authorizedUser, onAppend))
                    }
                    delete {
                        val onRemove = call.receive<List<LinkedRuleInputDto>>()
                        val authorizedUser = call.getAuthorized()
                        call.respond(userAccessControlService.removeRules(authorizedUser, onRemove))
                    }
                }
                route("roles") {
                    post("roles") {
                        val newRules = call.receive<List<Int>>()
                        val authorizedUser = call.getAuthorized()
                        call.respond<List<RoleOutputDto>>(userAccessControlService.addRoles(authorizedUser, newRules))
                    }
                    delete {
                        val onRemove = call.receive<List<Int>>()
                        val authorizedUser = call.getAuthorized()
                        call.respond(userAccessControlService.removeRoles(authorizedUser, onRemove))
                    }
                }
            }
        }
    }
}