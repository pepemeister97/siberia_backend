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
                get {
                    val authorizedUser = call.getAuthorized()
                    call.respond(transactionService.getAvailableTransactions(authorizedUser))
                }
                route("{transactionId}") {
                    get {
                        val authorizedUser = call.getAuthorized()
                        val transactionId = call.parameters["transactionId"]?.toInt() ?: throw BadRequestException("Transaction id must be INT")

                        val transactionFullOutputDto = transactionService.getOne(authorizedUser, transactionId)
                        transactionFullOutputDto.availableStatuses = transactionService.getAvailableStatuses(authorizedUser, transactionId)

                        call.respond(transactionFullOutputDto)
                    }
                }
            }
            route("income") {
                post {
                    val transactionInputDto = call.receive<TransactionInputDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond(transactionService.createIncomeTransaction(authorizedUser, transactionInputDto))
                }
                route("{transactionId}") {
                    patch("approve") {
                        val transactionId = call.parameters["transactionId"]?.toInt() ?: throw BadRequestException("Transaction id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond(transactionService.approveIncomeTransaction(authorizedUser, transactionId))
                    }
                    patch("cancel") {
                        val transactionId = call.parameters["transactionId"]?.toInt() ?: throw BadRequestException("Transaction id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond(transactionService.cancelIncomeTransaction(authorizedUser, transactionId))
                    }
                }
            }
            route("outcome") {
                post {
                    val transactionInputDto = call.receive<TransactionInputDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond(transactionService.createOutcomeTransaction(authorizedUser, transactionInputDto))
                }
                route("{transactionId}") {
                    patch("approve") {
                        val transactionId = call.parameters["transactionId"]?.toInt() ?: throw BadRequestException("Transaction id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond(transactionService.approveOutcomeTransaction(authorizedUser, transactionId))
                    }
                    patch("cancel") {
                        val transactionId = call.parameters["transactionId"]?.toInt() ?: throw BadRequestException("Transaction id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond(transactionService.cancelOutcomeTransaction(authorizedUser, transactionId))
                    }
                }
            }
            route("transfer") {
                post {
                    val transactionInputDto = call.receive<TransactionInputDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond(transactionService.createTransferTransaction(authorizedUser, transactionInputDto))
                }
                route("{transactionId}") {
                    patch("{statusId}") {
                        val transactionId = call.parameters["transactionId"]?.toInt() ?: throw BadRequestException("Transaction id must be INT")
                        val statusId = call.parameters["statusId"]?.toInt() ?: throw BadRequestException("Status id must be INT")
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
                    patch("{statusId}/{stockId}") {
                        val transactionId = call.parameters["transactionId"]?.toInt() ?: throw BadRequestException("Transaction id must be INT")
                        val statusId = call.parameters["statusId"]?.toInt() ?: throw BadRequestException("Status id must be INT")
                        val stockId = call.parameters["stockId"]?.toInt() ?: throw BadRequestException("Stock id must be INT")
                        val authorizedUser = call.getAuthorized()

                        if (statusId == AppConf.requestStatus.inProgress) {
                            call.respond(transactionService.startProcessTransferTransaction(authorizedUser, transactionId, stockId))
                        } else {
                            throw BadRequestException("Bad status provided")
                        }
                    }
                    patch("solve/{statusId}") {
                        val transactionId = call.parameters["transactionId"]?.toInt() ?: throw BadRequestException("Transaction id must be INT")
                        val statusId = call.parameters["statusId"]?.toInt() ?: throw BadRequestException("Status id must be INT")
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