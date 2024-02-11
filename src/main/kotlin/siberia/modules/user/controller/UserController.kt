package siberia.modules.user.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.ForbiddenException
import siberia.modules.rbac.data.dto.LinkedRuleInputDto
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.user.data.dto.CreateUserDto
import siberia.modules.user.data.dto.UserFilterDto
import siberia.modules.user.data.dto.UserOutputDto
import siberia.modules.user.data.dto.UserUpdateDto
import siberia.modules.user.service.UserAccessControlService
import siberia.modules.user.service.UserEventService
import siberia.modules.user.service.UserService
import siberia.utils.kodein.KodeinController

class UserController(override val di: DI) : KodeinController() {
    private val userAccessControlService: UserAccessControlService by instance()
    private val userService: UserService by instance()
    private val userEventService: UserEventService by instance()

    override fun Routing.registerRoutes() {
        authenticate("default") {
            route("user") {
                post("all") {
                    val userFilterDto = call.receive<UserFilterDto>()

                    call.respond<List<UserOutputDto>>(userService.getByFilter(userFilterDto))
                }
                get {
                    val authorizedUser = call.getAuthorized()

                    call.respond<UserOutputDto>(userService.getOne(authorizedUser.id))
                }
                get("roles") {
                    val authorizedUser = call.getAuthorized()
                    call.respond<List<RoleOutputDto>>(userAccessControlService.getUserRoles(authorizedUser))
                }
                get("rules") {
                    val authorizedUser = call.getAuthorized()
                    call.respond<List<LinkedRuleOutputDto>>(userAccessControlService.getUserRules(authorizedUser))
                }
            }
        }

        authenticate("user-managing") {
            route("user") {
                post {
                    val createUserDto = call.receive<CreateUserDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond(userService.createUser(authorizedUser, createUserDto))
                }
                post("rollback/{eventId}") {
                    val authorizedUser = call.getAuthorized()
                    val eventId = call.parameters.getInt("eventId", "Event id must be INT")

                    call.respond(userEventService.rollback(authorizedUser, eventId))
                }
                route("{userId}") {
                    get {
                        val userId = call.parameters.getInt("userId", "User id must be INT")

                        call.respond<UserOutputDto>(userService.getOne(userId))
                    }
                    delete {
                        val authorizedUser = call.getAuthorized()
                        val userId = call.parameters.getInt("userId", "User id must be INT")
                        if (userId == authorizedUser.id)
                            throw ForbiddenException()
                        call.respond(userService.removeUser(authorizedUser, userId))
                    }
                    patch {
                        val userUpdateDto = call.receive<UserUpdateDto>()
                        val authorizedUser = call.getAuthorized()
                        val userId = call.parameters.getInt("userId", "User id must be INT")

                        call.respond<UserOutputDto>(userService.updateUser(authorizedUser, userId, userUpdateDto))
                    }
                    route("rules") {
                        get {
                            val userId = call.parameters.getInt("userId", "User id must be INT")

                            call.respond<List<LinkedRuleOutputDto>>(userAccessControlService.getUserRules(userId))
                        }
                        post {
                            val onAppend = call.receive<List<LinkedRuleInputDto>>()
                            val authorizedUser = call.getAuthorized()
                            val userId = call.parameters.getInt("userId", "User id must be INT")

                            call.respond<List<LinkedRuleOutputDto>>(userAccessControlService.addRules(authorizedUser, userId, onAppend))
                        }
                        delete {
                            val onRemove = call.receive<List<LinkedRuleInputDto>>()
                            val authorizedUser = call.getAuthorized()
                            val userId = call.parameters.getInt("userId", "User id must be INT")

                            call.respond(userAccessControlService.removeRules(authorizedUser, userId, onRemove))
                        }
                    }
                    route("roles") {
                        get {
                            val userId = call.parameters.getInt("userId", "User id must be INT")

                            call.respond<List<RoleOutputDto>>(userAccessControlService.getUserRoles(userId))
                        }
                        post {
                            val newRules = call.receive<List<Int>>()
                            val authorizedUser = call.getAuthorized()
                            val userId = call.parameters.getInt("userId", "User id must be INT")

                            call.respond<List<RoleOutputDto>>(userAccessControlService.addRoles(authorizedUser, userId, newRules))
                        }
                        delete {
                            val onRemove = call.receive<List<Int>>()
                            val authorizedUser = call.getAuthorized()
                            val userId = call.parameters.getInt("userId", "User id must be INT")

                            call.respond(userAccessControlService.removeRoles(authorizedUser, userId, onRemove))
                        }
                    }
                }
            }
        }
    }
}