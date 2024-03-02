package siberia.modules.transaction.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.service.WriteOffTransactionService
import siberia.utils.kodein.KodeinController

class WriteOffTransactionController(override val di: DI) : KodeinController() {
    private val writeOffTransactionService: WriteOffTransactionService by instance()
    override fun Routing.registerRoutes() {
        route("transaction/write-off") {
            authenticate ("create-write-off") {
                post {
                    val authorizedUser = call.getAuthorized()
                    val transactionInputDto = call.receive<TransactionInputDto>()

                    call.respond(writeOffTransactionService.create(authorizedUser, transactionInputDto))
                }
            }

            authenticate ("approve-write-off") {
                route("{transactionId}") {
                    patch("approve") {
                        val authorizedUser = call.getAuthorized()
                        val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")

                        call.respond(writeOffTransactionService.processed(authorizedUser, transactionId))
                    }
                    patch("cancel") {
                        val authorizedUser = call.getAuthorized()
                        val transactionId = call.parameters.getInt("transactionId", "Transaction id must be INT")

                        call.respond(writeOffTransactionService.cancelCreation(authorizedUser, transactionId))
                    }
                }
            }
        }
    }
}