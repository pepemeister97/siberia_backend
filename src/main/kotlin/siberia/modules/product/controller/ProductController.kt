package siberia.modules.product.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.product.data.dto.ProductCreateDto
import siberia.modules.product.data.dto.ProductSearchDto
import siberia.modules.product.data.dto.ProductUpdateDto
import siberia.modules.product.service.ProductEventService
import siberia.modules.product.service.ProductService
import siberia.utils.kodein.KodeinController

class ProductController(override val di: DI) : KodeinController() {
    private val productService: ProductService by instance()
    private val productEventService: ProductEventService by instance()
    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        route("product") {
            authenticate("default") {
                post("all") {
                    val searchFilterDto = call.receive<ProductSearchDto>()

                    call.respond(productService.getByFilter(searchFilterDto))
                }
            }
            authenticate ("view-products-list") {
                get("{productId}") {
                    val productId = call.parameters.getInt("productId", "Product id must be INT")

                    call.respond(productService.getOne(productId))
                }
            }
            authenticate("products-managing") {
                post {
                    val productCreateDto = call.receive<ProductCreateDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond(productService.create(authorizedUser, productCreateDto))
                }
                post ("rollback/{eventId}") {
                    val authorizedUser = call.getAuthorized()
                    val eventId = call.parameters.getInt("eventId", "Event id must be INT")

                    call.respond(productEventService.rollback(authorizedUser, eventId))
                }
                route("{productId}") {
                    delete {
                        val productId = call.parameters.getInt("productId", "Product id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond(productService.remove(authorizedUser, productId))
                    }
                    patch {
                        val productUpdateDto = call.receive<ProductUpdateDto>()
                        val authorizedUser = call.getAuthorized()
                        val productId = call.parameters.getInt("productId", "Product id must be INT")

                        call.respond(productService.update(authorizedUser, productId, productUpdateDto))
                    }
                }
            }
        }
    }
}