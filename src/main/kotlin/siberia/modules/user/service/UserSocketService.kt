package siberia.modules.user.service

import io.ktor.websocket.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.plugins.Logger
import siberia.utils.kodein.KodeinService
import siberia.utils.websockets.WebSocketRegister
import siberia.utils.websockets.dto.WebSocketResponseDto

class UserSocketService (di: DI) : KodeinService(di) {
    private val webSocketRegister: WebSocketRegister by instance()

    fun deleteConnections(
        users : List<Int>
    ) {
        webSocketRegister.emit { connectionsRegister ->
            val connectionsByUser = connectionsRegister[users]
            Logger.debug("Users on delete", "main")
            Logger.debug(connectionsByUser, "main")
            Logger.debug("IDs:", "main")
            Logger.debug(users, "main")
            connectionsByUser.forEach {
                it.value.forEach { connection ->
                    connection.send(WebSocketResponseDto.wrap("logout"))
                    connection.close(CloseReason.Codes.CANNOT_ACCEPT, "")
                }
            }
        }
    }
    fun deleteConnection(
        userId: Int
    ) = deleteConnections(listOf(userId))
}