package siberia.modules.user.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.auth.data.dto.LinkedRuleInputDto
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.dto.RoleOutputDto
import siberia.modules.user.data.dto.CreateUserDto
import siberia.modules.user.data.dto.UserOutputDto
import siberia.modules.user.data.dto.UserPatchDto
import siberia.modules.user.service.UserAccessControlService
import siberia.modules.user.service.UserService
import siberia.utils.kodein.KodeinController

class UserController(override val di: DI) : KodeinController() {
    private val userAccessControlService: UserAccessControlService by instance()
    private val userService: UserService by instance()

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
        }

        authenticate("user-managing") {
            route("user") {
                post {
                    val createUserDto = call.receive<CreateUserDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond(userService.createUser(authorizedUser, createUserDto))
                }
                delete("{userId}") {
                    val authorizedUser = call.getAuthorized()
                    val userId = call.parameters["userId"]?.toInt() ?: throw BadRequestException("User id must be INT")

                    call.respond(userService.removeUser(authorizedUser, userId))
                }
                patch("{userId}") {
                    val authorizedUser = call.getAuthorized()
                    val userId = call.parameters["userId"]?.toInt() ?: throw BadRequestException("User id must be INT")
                    val userPatchDto = call.receive<UserPatchDto>()

                    call.respond<UserOutputDto>(userService.updateUser(authorizedUser, userId, userPatchDto))
                }
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