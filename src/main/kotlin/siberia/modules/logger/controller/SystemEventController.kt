package siberia.modules.logger.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.logger.data.dto.SystemEventSearchFilter
import siberia.modules.logger.service.SystemEventService
import siberia.utils.kodein.KodeinController

class SystemEventController(override val di: DI) : KodeinController() {
    private val systemEventService: SystemEventService by instance()

    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        route("logs") {
            authenticate("check-logs") {
                post {
                    val filter = call.receive<SystemEventSearchFilter>()

                    call.respond(systemEventService.getByFilter(filter))
                }
            }
            authenticate("default") {
                get("types") {
                    call.respond(systemEventService.getAllTypes())
                }
                get("object/types") {
                    call.respond(systemEventService.getAllObjectTypes())
                }
            }
        }
    }

}