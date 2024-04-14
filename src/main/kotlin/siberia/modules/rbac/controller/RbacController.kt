package siberia.modules.rbac.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.rbac.data.dto.LinkedRuleInputDto
import siberia.modules.rbac.data.dto.RoleFilterDto
import siberia.modules.rbac.data.dto.RoleCreateDto
import siberia.modules.rbac.data.dto.RoleUpdateDto
import siberia.modules.rbac.service.RbacService
import siberia.modules.rbac.service.RoleEventService
import siberia.modules.rbac.service.RoleRulesEventService
import siberia.utils.kodein.KodeinController

class RbacController(override val di: DI) : KodeinController() {
    private val rbacService: RbacService by instance()
    private val roleEventService: RoleEventService by instance()
    private val roleRulesEventService: RoleRulesEventService by instance()
    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        authenticate("rbac-managing") {
            route("rbac") {
                route("roles") {
                    post("all") {
                        val roleFilterDto = call.receive<RoleFilterDto>()

                        call.respond(rbacService.getFiltered(roleFilterDto))
                    }
                    post {
                        val roleCreateDto = call.receive<RoleCreateDto>()
                        val authorizedUser = call.getAuthorized()

                        call.respond(rbacService.createRole(authorizedUser, roleCreateDto))
                    }
                    post("rollback/{eventId}") {
                        val authorizedUser = call.getAuthorized()
                        val eventId = call.parameters.getInt("eventId", "Event id must be INT")

                        call.respond(roleEventService.rollback(authorizedUser, eventId))
                    }
                    post("rules/rollback/{eventId}") {
                        val authorizedUser = call.getAuthorized()
                        val eventId = call.parameters.getInt("eventId", "Event id must be INT")

                        call.respond(roleRulesEventService.rollback(authorizedUser, eventId))
                    }
                    route("{roleId}") {
                        get {
                            val roleId = call.parameters.getInt("roleId", "Role id must be INT")
                            call.respond(rbacService.getRole(roleId))
                        }
                        post("rules") {
                            val onAppend = call.receive<List<LinkedRuleInputDto>>()
                            val roleId = call.parameters.getInt("roleId", "Role id must be INT")
                            val authorizedUser = call.getAuthorized()

                            call.respond(rbacService.appendRulesToRole(authorizedUser, roleId, onAppend))
                        }
                        delete("rules") {
                            val onRemove = call.receive<List<LinkedRuleInputDto>>()
                            val roleId = call.parameters.getInt("roleId", "Role id must be INT")
                            val authorizedUser = call.getAuthorized()

                            call.respond(rbacService.removeRulesFromRole(authorizedUser, roleId, onRemove))
                        }
                        patch {
                            val roleUpdateDto = call.receive<RoleUpdateDto>()
                            val roleId = call.parameters.getInt("roleId", "Role id must be INT")
                            val authorizedUser = call.getAuthorized()

                            call.respond(rbacService.updateRole(authorizedUser, roleId, roleUpdateDto))
                        }
                        delete {
                            val roleId = call.parameters.getInt("roleId", "Role id must be INT")
                            val authorizedUser = call.getAuthorized()

                            call.respond(rbacService.removeRole(authorizedUser, roleId))
                        }
                    }
                }
            }
        }
        authenticate("default") {
            route("rules") {
                route("categories") {
                    get {
                        call.respond(rbacService.getAllCategories())
                    }
                }
                get {
                    call.respond(rbacService.getAllRules())
                }
            }
        }
    }
}