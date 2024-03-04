package siberia.modules.image.controller

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.image.data.dto.ImageCreateDto
import siberia.modules.image.data.dto.ImageSearchFilterDto
import siberia.modules.image.service.ImageService
import siberia.utils.kodein.KodeinController

class ImageController(override val di: DI) : KodeinController() {
    private val imageService : ImageService by instance()
    override fun Routing.registerRoutes() {
        route("images"){
            post {
                val images = call.receive<List<ImageCreateDto>>()
                val authorizedUser = call.getAuthorized()
                call.respond(imageService.create(authorizedUser, images))
            }
            get{
                val filter = call.receive<ImageSearchFilterDto>()
                call.respond(imageService.getAll(filter))
            }
            route("{imageId}"){
                delete {
                    val imageId = call.parameters.getInt("imageId", "Image id must be INT")
                    call.respond(imageService.remove(imageId))
                }
                get{
                    val imageId = call.parameters.getInt("imageId", "Image id must be INT")
                    call.respond(imageService.remove(imageId))
                }
            }

        }
    }
}