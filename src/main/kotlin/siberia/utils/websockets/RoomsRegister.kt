package siberia.utils.websockets

class RoomsRegister (
    private val rooms: MutableMap<String, WebSocketRoom> = mutableMapOf()
) {
    operator fun get(roomName: String): WebSocketRoom? = rooms[roomName]

    operator fun set(roomName: String, subscribers: ConnectionsRegister) {
        val room = rooms[roomName]
        if (room != null)
            room.subscribers merge subscribers
        else
            rooms[roomName] = WebSocketRoom(roomName, subscribers)
    }

    infix fun clearInactiveParticipantsOf(roomName: String) {
        val room = rooms[roomName] ?: return
        room.subscribers.clearInactive()
    }

}