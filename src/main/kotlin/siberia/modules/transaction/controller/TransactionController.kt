package siberia.modules.transaction.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.transaction.data.dto.TransactionSearchFilter
import siberia.modules.transaction.service.TransactionService
import siberia.utils.kodein.KodeinController

class TransactionController(override val di: DI) : KodeinController() {
    private val transactionService: TransactionService by instance()
    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        route("transaction") {
            authenticate ("default") {
                get("types") {
                    call.respond(transactionService.getAllTypes())
                }
                get("statuses") {
                    call.respond(transactionService.getAllStatuses())
                }
                post {
                    val authorizedUser = call.getAuthorized()
                    val transactionSearchFilter = call.receive<TransactionSearchFilter>()

                    call.respond(transactionService.getAvailableTransactions(authorizedUser, transactionSearchFilter))
                }
                authenticate("default") {
                    route("assembly") {
                        get {
                            val authorizedUser = call.getAuthorized()

                            call.respond(transactionService.getTransactionOnAssembly(authorizedUser))
                        }
                        route("products") {
                            post("sorted") {
                                val authorizedUser = call.getAuthorized()
                                val transactions = call.receive<List<Int>>()

                                call.respond(transactionService.getTransactions(authorizedUser, transactions))
                            }
                            post("list") {
                                val authorizedUser = call.getAuthorized()
                                val transactions = call.receive<List<Int>>()

                                call.respond(transactionService.getProductsFromTransactions(authorizedUser, transactions))
                            }
                        }
                    }
                }
                route("{transactionId}") {
                    get {
                        val authorizedUser = call.getAuthorized()
                        val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")

                        val transactionFullOutputDto = transactionService.getOne(authorizedUser, transactionId)
                        transactionFullOutputDto.availableStatuses = transactionService.getAvailableStatuses(authorizedUser, transactionId)

                        call.respond(transactionFullOutputDto)
                    }
                }
            }
            route("transfer") {
            }

        }
    }
}