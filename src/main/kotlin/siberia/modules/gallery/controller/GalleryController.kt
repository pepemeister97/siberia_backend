package siberia.modules.gallery.controller

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.gallery.data.dto.ImageCreateDto
import siberia.modules.gallery.data.dto.ImageSearchFilterDto
import siberia.modules.gallery.service.GalleryService
import siberia.utils.kodein.KodeinController

class GalleryController(override val di: DI) : KodeinController() {
    private val galleryService : GalleryService by instance()
    override fun Routing.registerRoutes() {
        route("images"){
            post {
                val images = call.receive<List<ImageCreateDto>>()
                val authorizedUser = call.getAuthorized()
                call.respond(galleryService.create(authorizedUser, images))
            }
            get{
                val filter = call.receive<ImageSearchFilterDto>()
                call.respond(galleryService.getAll(filter))
            }
            route("{imageId}"){
                delete {
                    val imageId = call.parameters.getInt("imageId", "Image id must be INT")
                    call.respond(galleryService.remove(imageId))
                }
                get{
                    val imageId = call.parameters.getInt("imageId", "Image id must be INT")
                    call.respond(galleryService.remove(imageId))
                }
            }

        }
    }
}