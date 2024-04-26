package siberia.modules.product.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.product.data.dto.*
import siberia.modules.product.service.ProductEventService
import siberia.modules.product.service.ProductMassiveEventService
import siberia.modules.product.service.ProductService
import siberia.utils.kodein.KodeinController
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import io.ktor.http.content.*
import siberia.modules.product.service.ProductParseService


class ProductController(override val di: DI) : KodeinController() {
    private val productService: ProductService by instance()
    private val productEventService: ProductEventService by instance()
    private val productMassiveEventService: ProductMassiveEventService by instance()

    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        route("product") {
            authenticate("default") {
                route("all") {
                    post {
                        val searchFilterDto = call.receive<ProductSearchDto>()

                        call.respond(productService.getByFilter(searchFilterDto).await())
                    }
                    post("unminified") {
                        val searchFilterDto = call.receive<ProductSearchDto>()

                        call.respond(productService.getUnminifiedList(searchFilterDto).await())
                    }
                }
                post("xls") {
                    val productGetXlsDto = call.receive<ProductGetXlsDto>()

                    val searchFilters = productGetXlsDto.searchFilters
                    val fieldsDemand = productGetXlsDto.fieldsDemand

                    call.respond(productService.getXls(searchFilters, fieldsDemand))
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
                post("/parse/{fileType}") {
                    val fileType = call.parameters["fileType"]
                    when (fileType) {
                        "csv" -> {
                            val bytes = call.receive<ByteArray>()
                            call.respond(productService.parseCsv(bytes))
                        }
                        "xlsx" -> {
                            val multipart = call.receiveMultipart()
                            multipart.forEachPart { part ->
                                when (part) {
                                    is PartData.FileItem -> {
                                        val inputStream = part.streamProvider()
                                        val workbook = XSSFWorkbook(inputStream)
                                        inputStream.close()
                                        call.respond(productService.parseXlsx(workbook))
                                    }
                                    else -> {
                                        throw BadRequestException("No file uploaded or multipart data is empty.")
                                    }
                                }
                                part.dispose()
                            }
                        }
                    }
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