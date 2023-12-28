package siberia.modules.brand.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.brand.data.dto.BrandInputDto
import siberia.modules.brand.service.BrandService
import siberia.utils.kodein.KodeinController

class BrandController(override val di: DI) : KodeinController() {
    private val brandService: BrandService by instance()
    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        route("brand") {
            authenticate ("brand-managing") {
                post {
                    val brandInputDto = call.receive<BrandInputDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond(brandService.create(authorizedUser, brandInputDto))
                }
                route("{brandId}") {
                    patch {
                        val brandInputDto = call.receive<BrandInputDto>()
                        val authorizedUser = call.getAuthorized()
                        val brandId = call.parameters.getInt("brandId", "Brand id must be INT")

                        call.respond(brandService.update(authorizedUser, brandId, brandInputDto))
                    }

                    delete {
                        val authorizedUser = call.getAuthorized()
                        val brandId = call.parameters.getInt("brandId", "Brand id must be INT")

                        call.respond(brandService.remove(authorizedUser, brandId))
                    }
                }
            }
            authenticate("default") {
                get {
                    call.respond(brandService.getAll())
                }

                get("{brandId}") {
                    val brandId = call.parameters.getInt("brandId", "Brand id must be INT")

                    call.respond(brandService.getOne(brandId))
                }
            }
        }
    }
}