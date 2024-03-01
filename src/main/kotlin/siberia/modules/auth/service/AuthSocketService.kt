package siberia.modules.auth.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.plugins.Logger
import siberia.utils.kodein.KodeinService
import siberia.utils.websockets.WebSocketRegister
import siberia.utils.websockets.dto.WebSocketResponseDto

class AuthSocketService(di: DI) : KodeinService(di) {
    private val webSocketRegister: WebSocketRegister by instance()
    fun updateRules(
        users: List<Int>
    ) {
        webSocketRegister.emit { connectionsRegister ->
            val connectionsByUser = connectionsRegister[users]
            Logger.debug("Connections by user", "main")
            Logger.debug(connectionsByUser.size, "main")
            Logger.debug(connectionsByUser, "main")
            connectionsByUser.forEach {
                it.value.forEach { connection ->
                    if (connection.isActive)
                        connection.send(WebSocketResponseDto.wrap("update-rules"))
                }
            }
        }
    }

    fun updateRules(
        userId: Int
    ) = updateRules(listOf(userId))
}