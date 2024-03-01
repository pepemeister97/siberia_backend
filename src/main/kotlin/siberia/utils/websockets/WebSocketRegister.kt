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
import siberia.utils.websockets.dto.RequestHandlerInput
import siberia.utils.websockets.dto.WebSocketRequestDto
import siberia.utils.websockets.dto.WebSocketResponseDto

typealias WebSocketEmitter = suspend (connectionsRegister: ConnectionsRegister) -> Unit

typealias WebSocketRequestHandler = WebSocketRegister.(
    requestHandlerInput: RequestHandlerInput
) -> Unit

class WebSocketRegister(override val di: DI) : KodeinController() {
    private val connections: ConnectionsRegister = ConnectionsRegister()
    private val rooms: RoomsRegister = RoomsRegister()
    private val routes: MutableMap<String, MutableList<WebSocketRequestHandler>> = mutableMapOf()
    private val json = Json { ignoreUnknownKeys = true }

    init {
        registerRoutes("connect") {
            input ->
                connections[input.authorizedUser.id] = input.socketSession
                //After every new connection check for inactive ones
                connections clearInactiveBy input.authorizedUser.id
        }
        registerRoutes("connect-to-room") {
            input ->
                val roomName = input.request.body
                rooms[roomName] = ConnectionsRegister(input.authorizedUser.id, input.socketSession)
                //Clear participants of room who are inactive
                rooms clearInactiveParticipantsOf roomName
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

    fun emit(roomName: String, emitter: WebSocketEmitter) {
        val newEmit = WebSocketEvent.NewRoomEmit(roomName, emitter)
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

        class NewRoomEmit(
            val roomName: String,
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
                        it(RequestHandlerInput(request, authorizedUser, socketSession, connections, rooms))
                    }
                }
                is WebSocketEvent.NewRoomEmit -> with(event) {
                    val room = rooms[roomName] ?: return@with
                    emitter(room.subscribers)
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