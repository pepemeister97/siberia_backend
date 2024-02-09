package siberia.utils.websockets

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.plugins.Logger
import siberia.utils.kodein.KodeinController
import siberia.utils.security.jwt.JwtUtil
import siberia.utils.websockets.dto.WebSocketRequestDto
import siberia.utils.websockets.dto.WebSocketResponseDto


typealias WebSocketConnectionsRegister = MutableMap<Int, MutableList<DefaultWebSocketSession>>

typealias WebSocketEmitter = suspend (connectionsRegister: WebSocketConnectionsRegister) -> Unit

typealias WebSocketRequestHandler = (
    request: WebSocketRequestDto,
    authorizedUser: AuthorizedUser,
    socketSession: DefaultWebSocketSession,
    actualConnections: WebSocketConnectionsRegister
) -> Unit

class WebSocketRegister(override val di: DI) : KodeinController() {
    private val connections: WebSocketConnectionsRegister = mutableMapOf()
    private val routes: MutableMap<String, MutableList<WebSocketRequestHandler>> = mutableMapOf()
    private val json = Json { ignoreUnknownKeys = true }

    init {
        registerRoutes("connect") {
            _, authorizedUser: AuthorizedUser, socketSession: DefaultWebSocketSession, _ ->
                val connectionsByUser = connections[authorizedUser.id]
                if (connectionsByUser != null)
                    connectionsByUser.add(socketSession)
                else
                    connections[authorizedUser.id] = mutableListOf(socketSession)

                //After every new connection check for inactive ones
                connections[authorizedUser.id] =
                    connections[authorizedUser.id]?.filter {
                        it.isActive
                    }?.toMutableList() ?: mutableListOf()
        }
    }

    fun registerRoutes(type: String, handler: WebSocketRequestHandler) {
        if (routes.containsKey(type))
            routes[type]?.add(handler)
        else
            routes[type] = mutableListOf(handler)
    }

    fun emit(emitter: WebSocketEmitter) {
        val newEmit = WebSocketEvent.NewEmit(emitter)
        websocketChannel.trySend(newEmit)
    }

    private open class WebSocketEvent {
        class NewRequest (
            val request: WebSocketRequestDto,
            val authorizedUser: AuthorizedUser,
            val socketSession: DefaultWebSocketSession,
        ): WebSocketEvent()

        class NewEmit(
            val emitter: WebSocketEmitter
        ): WebSocketEvent()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private val websocketChannel = CoroutineScope(Job()).actor<WebSocketEvent>(capacity = Channel.BUFFERED) {
        for (event in this) {
            when (event) {
                //When we need to emit something from system to the audience
                is WebSocketEvent.NewEmit -> with(event) {
                    emitter(connections)
                }
                //When we need to handle something from audience in system
                is WebSocketEvent.NewRequest -> with(event) {
                    val handler = routes[request.headers.uri] ?: return@with
                    handler.forEach {
                        it(request, authorizedUser, socketSession, connections)
                    }
                }
            }
        }
    }

    private fun authorize(requestDto: WebSocketRequestDto): AuthorizedUser =
        JwtUtil.verifyNative(requestDto.headers.authorization)

    private suspend fun closeForbidden(socketSession: DefaultWebSocketSession) {
        socketSession.close(CloseReason(403, "Forbidden"))
    }

    private suspend fun websocketBadRequest(message: String, socketSession: DefaultWebSocketSession) {
        socketSession.send(
            Frame.Text(
                WebSocketResponseDto.wrap("bad-request").json
            )
        )
    }

    private suspend fun websocketRoutesProcessor(requestDto: WebSocketRequestDto, socketSession: DefaultWebSocketSession) {
        val authorizedUser = try {
            authorize(requestDto)
        } catch (e: Exception) {
            Logger.debugException("Websocket exception", e, "main")
            closeForbidden(socketSession)
            throw ForbiddenException()
        }
        val request = WebSocketEvent.NewRequest(requestDto, authorizedUser, socketSession)
        websocketChannel.send(request)
    }

    override fun Routing.registerRoutes() {
        webSocket("/ws") {
            for (frame in incoming) {
                Logger.debug("Websocket new frame", "websocket")
                Logger.debug(frame, "websocket")
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