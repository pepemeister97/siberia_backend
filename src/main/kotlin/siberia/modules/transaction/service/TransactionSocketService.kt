package siberia.modules.transaction.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.transaction.data.dto.TransactionFullOutputDto
import siberia.utils.kodein.KodeinService
import siberia.utils.websockets.WebSocketRegister
import siberia.utils.websockets.dto.WebSocketResponseDto

class TransactionSocketService(di: DI) : KodeinService(di) {
    private val webSocketRegister: WebSocketRegister by instance()
    fun updateStatus(
        transactionOutputDto: TransactionFullOutputDto
    ) {
        webSocketRegister.emit { connectionsRegister ->
            connectionsRegister.all().forEach {
                it.value.forEach { connection ->
                    if (connection.isActive)
                        connection.send(WebSocketResponseDto.wrap<TransactionFullOutputDto>("update-transaction", transactionOutputDto))
                }
            }
        }
    }
}