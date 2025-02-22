package siberia.modules.transaction.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.service.OutcomeTransactionService
import siberia.utils.kodein.KodeinController

class OutcomeTransactionController(override val di: DI) : KodeinController() {
    private val outcomeTransactionService: OutcomeTransactionService by instance()
    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        route("transaction/outcome") {
            authenticate ("create-outcome-request") {
                post {
                    val authorizedUser = call.getAuthorized()
                    val transactionInputDto = call.receive<TransactionInputDto>()

                    call.respond(outcomeTransactionService.create(authorizedUser, transactionInputDto))
                }
                post ("{transactionId}") {
                    val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                    val authorizedUser = call.getAuthorized()

                    call.respond(outcomeTransactionService.createFromHidden(authorizedUser, transactionId))
                }
                route("hidden") {
                    post {
                        val authorizedUser = call.getAuthorized()
                        val transactionInputDto = call.receive<TransactionInputDto>()
                        transactionInputDto.hidden = true

                        call.respond(outcomeTransactionService.create(authorizedUser, transactionInputDto))
                    }
                    route("{transactionId}") {
                        delete {
                            val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")

                            call.respond(outcomeTransactionService.removeHidden(transactionId))
                        }
                    }
                }

            }
            authenticate("approve-outcome-request") {
                route("{transactionId}") {
                    patch {
                        val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                        val products = call.receive<List<TransactionInputDto.TransactionProductInputDto>>()

                        call.respond(outcomeTransactionService.updateHidden(transactionId, products))
                    }
                    patch("approve") {
                        val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond(outcomeTransactionService.processed(authorizedUser, transactionId))
                    }
                    patch("cancel") {
                        val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond(outcomeTransactionService.cancelCreation(authorizedUser, transactionId))
                    }
                }
            }
        }
    }
}