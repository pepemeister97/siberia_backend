package siberia.modules.notifications.controller

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.notifications.service.NotificationService
import siberia.plugins.Logger
import siberia.utils.kodein.KodeinController
import siberia.utils.security.jwt.JwtUtil
import siberia.utils.websockets.dto.WebSocketRequestDto
import siberia.utils.websockets.dto.WebSocketResponseDto

class NotificationsWebSocketController(override val di: DI) : KodeinController() {
    private val notificationService: NotificationService by instance()
    private val json = Json { ignoreUnknownKeys = true }

    private fun authorize(requestDto: WebSocketRequestDto): AuthorizedUser =
        JwtUtil.verifyNative(requestDto.headers.authorization)

    private suspend fun closeForbidden(socketSession: DefaultWebSocketSession) {
        socketSession.close(CloseReason(403, "Forbidden"))
    }

    private suspend fun websocketBadRequest(message: String, socketSession: DefaultWebSocketSession) {
        socketSession.send(
            Frame.Text(
                WebSocketResponseDto.wrap("bad-request", BadRequestException(message)).json
            )
        )
    }

    private suspend fun websocketRoutesProcessor(requestDto: WebSocketRequestDto, socketSession: DefaultWebSocketSession) {
        val authorizedUser = try {
            authorize(requestDto)
        } catch (e: Exception) {
            closeForbidden(socketSession)
            throw ForbiddenException()
        }
        when (requestDto.headers.uri) {
            "connect" -> {
                notificationService.newConnection(authorizedUser, socketSession)
            }
        }
    }
    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        webSocket("/ws") { // websocketSession
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    val request = json.decodeFromString<WebSocketRequestDto>(message)
                    try {
                        websocketRoutesProcessor(request, this)
                    } catch (e: Exception) {
                        Logger.debug("WebSocket request: $request was failed", "websocket")
                    }
                }
            }
        }
    }
}