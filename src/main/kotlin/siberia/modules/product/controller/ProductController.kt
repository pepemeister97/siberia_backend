package siberia.modules.product.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.product.data.dto.ProductCreateDto
import siberia.modules.product.data.dto.ProductListItemOutputDto
import siberia.modules.product.data.dto.ProductSearchDto
import siberia.modules.product.data.dto.ProductUpdateDto
import siberia.modules.product.service.ProductEventService
import siberia.modules.product.service.ProductMassiveEventService
import siberia.modules.product.service.ProductParseService
import siberia.modules.product.service.ProductService
import siberia.utils.kodein.KodeinController

class ProductController(override val di: DI) : KodeinController() {
    private val productService: ProductService by instance()
    private val productEventService: ProductEventService by instance()
    private val productParseService: ProductParseService by instance()
    private val productMassiveEventService: ProductMassiveEventService by instance()

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
                post ("parse/csv") {
                    val bytes = call.receive<ByteArray>()
                    call.respond(productParseService.parseCSVtoProductDto(bytes))
                }
                route("bulk") {
                    post {
                        val products = call.receive<List<ProductCreateDto>>()
                        val authorizedUser = call.getAuthorized()
                        call.respond(productService.bulkInsert(authorizedUser, products))
                    }
                    post("rollback/{eventId}") {
                        val eventId = call.parameters.getInt("eventId", "Event id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond(productMassiveEventService.rollback(authorizedUser, eventId))
                    }
                }
            }
            authenticate ("mobile-access") {
                post("search") {
                    val authorizedUser = call.getAuthorized()
                    val searchFilterDto = call.receive<ProductSearchDto>()

                    call.respond<List<ProductListItemOutputDto>>(productService.getAvailableByFilter(authorizedUser, searchFilterDto))
                }
            }
            route("bar/{barCode}") {
                authenticate("mobile-access") {
                    get {
                        val barCode = call.parameters["barCode"] ?: throw BadRequestException("BarCode must be provided")

                        call.respond(productService.getByBarCode(barCode))
                    }
                }
            }
            route("{productId}") {
                authenticate("mobile-access") {
                    get("availability") {
                        val productId = call.parameters.getInt("productId", "Product id must be INT")

                        call.respond(productService.getAvailability(productId))
                    }
                }
                authenticate ("view-products-list") {
                    get {
                        val productId = call.parameters.getInt("productId", "Product id must be INT")

                        call.respond(productService.getOne(productId))
                    }
                }
                authenticate("products-managing") {
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