package siberia.modules.stock.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.stock.data.dto.StockCreateDto
import siberia.modules.stock.data.dto.StockSearchDto
import siberia.modules.stock.data.dto.StockUpdateDto
import siberia.modules.stock.service.StockCache
import siberia.modules.stock.service.StockEventService
import siberia.modules.stock.service.StockService
import siberia.utils.kodein.KodeinController

class StockController(override val di: DI) : KodeinController() {
    private val stockService: StockService by instance()
    private val stockEventService: StockEventService by instance()

    override fun Routing.registerRoutes() {
        route("stock") {
            authenticate ("default") {
                get("all/input") {
                    call.respond(StockCache.tryGetAllInput {
                        stockService.getAll()
                    })
                }
                post("all") {
                    val stockSearchDto = call.receive<StockSearchDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond(stockService.getAvailableByFilter(authorizedUser, stockSearchDto))
                }
                get("{stockId}") {
                    val stockId = call.parameters.getInt("stockId", "Stock id must be INT")
                    val authorizedUser = call.getAuthorized()

                    call.respond(stockService.getOne(authorizedUser, stockId))
                }
            }
            authenticate ("stock-managing") {
                post {
                    val stockCreateDto = call.receive<StockCreateDto>()
                    val authorizedUser = call.getAuthorized()

                    StockCache.invalidate()

                    call.respond(stockService.create(authorizedUser, stockCreateDto))
                }
                post ("rollback/{eventId}") {
                    val authorizedUser = call.getAuthorized()
                    val eventId = call.parameters.getInt("eventId", "Event id must be int")

                    StockCache.invalidate()

                    call.respond(stockEventService.rollback(authorizedUser, eventId))
                }
                route("{stockId}") {
                    delete {
                        val authorizedUser = call.getAuthorized()
                        val stockId = call.parameters.getInt("stockId", "Stock id must be INT")

                        StockCache.invalidate()

                        call.respond(stockService.remove(authorizedUser, stockId))
                    }
                    patch {
                        val authorizedUser = call.getAuthorized()
                        val stockUpdateDto = call.receive<StockUpdateDto>()
                        val stockId = call.parameters.getInt("stockId", "Stock id must be INT")

                        StockCache.invalidate()

                        call.respond(stockService.update(authorizedUser, stockId, stockUpdateDto))
                    }
                }
            }
        }
    }
}