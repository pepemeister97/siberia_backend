package siberia.modules.transaction.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.modules.transaction.data.dto.TransactionInputDto
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
                post {
                    val authorizedUser = call.getAuthorized()
                    val transactionSearchFilter = call.receive<TransactionSearchFilter>()

                    call.respond(transactionService.getAvailableTransactions(authorizedUser, transactionSearchFilter))
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
            route("income") {
                authenticate ("create-income-request") {
                    post {
                        val transactionInputDto = call.receive<TransactionInputDto>()
                        val authorizedUser = call.getAuthorized()

                        call.respond(transactionService.createIncomeTransaction(authorizedUser, transactionInputDto))
                    }
                }
                authenticate ("approve-income-request") {
                    route("{transactionId}") {
                        patch("approve") {
                            val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                            val authorizedUser = call.getAuthorized()

                            call.respond(transactionService.approveIncomeTransaction(authorizedUser, transactionId))
                        }
                        patch("cancel") {
                            val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                            val authorizedUser = call.getAuthorized()

                            call.respond(transactionService.cancelIncomeTransaction(authorizedUser, transactionId))
                        }
                    }
                }
            }
            route("outcome") {
                authenticate ("create-outcome-request") {
                    post {
                        val transactionInputDto = call.receive<TransactionInputDto>()
                        val authorizedUser = call.getAuthorized()

                        call.respond(transactionService.createOutcomeTransaction(authorizedUser, transactionInputDto))
                    }
                }
                authenticate("approve-outcome-request") {
                    route("{transactionId}") {
                        patch("approve") {
                            val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                            val authorizedUser = call.getAuthorized()

                            call.respond(transactionService.approveOutcomeTransaction(authorizedUser, transactionId))
                        }
                        patch("cancel") {
                            val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                            val authorizedUser = call.getAuthorized()

                            call.respond(transactionService.cancelOutcomeTransaction(authorizedUser, transactionId))
                        }
                    }
                }
            }
            route("transfer") {
                authenticate ("create-transfer-request") {
                    post {
                        val transactionInputDto = call.receive<TransactionInputDto>()
                        val authorizedUser = call.getAuthorized()

                        call.respond(transactionService.createTransferTransaction(authorizedUser, transactionInputDto))
                    }
                }
                authenticate ("default") {
                    route("{transactionId}") {
                        patch("{statusId}") {
                            val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                            val statusId = call.parameters.getInt("statusId", "Status id must be INT")
                            val authorizedUser = call.getAuthorized()

                            when (statusId) {
                                AppConf.requestStatus.open -> call.respond(transactionService.approveTransferTransactionCreation(authorizedUser, transactionId))
                                AppConf.requestStatus.creationCancelled ->  call.respond(transactionService.cancelTransferTransactionCreation(authorizedUser, transactionId))
                                AppConf.requestStatus.processingCancelled -> call.respond(transactionService.cancelProcessingTransferTransaction(authorizedUser, transactionId))
                                AppConf.requestStatus.delivered -> call.respond(transactionService.approveTransferDelivery(authorizedUser, transactionId))
                                AppConf.requestStatus.notDelivered -> call.respond(transactionService.markAsNotDelivered(authorizedUser, transactionId))
                                else -> throw BadRequestException("Bad status provided")
                            }
                        }
                        patch("${AppConf.requestStatus.inProgress}/{stockId}") {
                            val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                            val stockId = call.parameters.getInt("stockId", "Stock id must be INT")

                            val authorizedUser = call.getAuthorized()

                            call.respond(transactionService.startProcessTransferTransaction(authorizedUser, transactionId, stockId))
                        }
                        authenticate ("solve-not-delivered-problem") {
                            patch("solve/{statusId}") {
                                val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")
                                val statusId = call.parameters.getInt("statusId", "Status id must be INT")
                                val authorizedUser = call.getAuthorized()

                                val availableToSolve = listOf(
                                    AppConf.requestStatus.delivered, AppConf.requestStatus.deliveryCancelled, AppConf.requestStatus.failed
                                )

                                if (availableToSolve.contains(statusId))
                                    call.respond(transactionService.solveNotDelivered(authorizedUser, transactionId, statusId))
                                else
                                    throw BadRequestException("Bad status provided")
                            }
                        }
                    }
                }
            }

        }
    }
}