package siberia.utils.websockets.dto

import io.ktor.websocket.*
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.utils.websockets.ConnectionsRegister
import siberia.utils.websockets.RoomsRegister

data class RequestHandlerInput (
    val request: WebSocketRequestDto,
    val authorizedUser: AuthorizedUser,
    val socketSession: DefaultWebSocketSession,
    val actualConnections: ConnectionsRegister,
    val roomsRegister: RoomsRegister
)