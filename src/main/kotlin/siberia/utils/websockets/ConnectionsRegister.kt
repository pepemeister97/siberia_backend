package siberia.utils.websockets

import io.ktor.websocket.*

class ConnectionsRegister (
    private var connections: MutableMap<Int, MutableList<WebSocketSession>> = mutableMapOf()
) {
    constructor(userId: Int, socketSession: DefaultWebSocketSession) : this() {
        this[userId] = socketSession
    }
    operator fun get(userId: Int): MutableList<WebSocketSession>? = connections[userId]

    operator fun get(users: List<Int>): Map<Int, MutableList<WebSocketSession>> {
        return connections.filterKeys { users.contains(it) }
    }

    operator fun set(userId: Int, socketSession: DefaultWebSocketSession) {
        val connectionsByUser = connections[userId]
        if (connectionsByUser != null)
            connectionsByUser.add(WebSocketSession(socketSession))
        else
            this.connections[userId] = mutableListOf(WebSocketSession(socketSession))
    }

    operator fun set(userId: Int, connections: MutableList<WebSocketSession>) {
        this.connections[userId] = connections
    }

    fun clearInactive() {
        connections = connections.map {
            Pair(it.key, it.value.filter { session -> session.isActive }.toMutableList())
        }.toMap().toMutableMap()
    }

    infix fun clearInactiveBy(userId: Int) {
        connections[userId] =
            connections[userId]?.filter {
                it.isActive
            }?.toMutableList() ?: mutableListOf()
    }

    infix fun merge(connectionsRegister: ConnectionsRegister) {
        connectionsRegister.forEach {
            this[it.key] = it.value
        }
    }

    fun <T : Any> map(handler: (Map.Entry<Int, MutableList<WebSocketSession>>) -> T): List<T> {
        return connections.map(handler)
    }

    fun forEach(handler: (Map.Entry<Int, MutableList<WebSocketSession>>) -> Unit) {
        connections.forEach(handler)
    }
}