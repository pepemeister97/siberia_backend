package siberia.modules.notifications.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.notifications.data.dto.NotificationsFilterDto
import siberia.modules.notifications.service.NotificationService
import siberia.utils.kodein.KodeinController

class NotificationsController(override val di: DI) : KodeinController() {
    private val notificationService: NotificationService by instance()
    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        authenticate("default") {
            post("/all") {
                val authorizedUser = call.getAuthorized()
                val notificationFilterDto = call.receive<NotificationsFilterDto>()

                call.respond(notificationService.getNotifications(authorizedUser.id, notificationFilterDto))
            }

            post("/watch") {
                val authorizedUser = call.getAuthorized()
                val onWatch = call.receive<List<Int>>()

                call.respond(notificationService.setWatched(authorizedUser.id, onWatch))
            }
        }
    }
}