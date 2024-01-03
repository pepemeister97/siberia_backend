package siberia.utils.websockets.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
data class WebSocketResponseDto <T> (
    val type: String,
    val data: T
) {
    @Transient private val serializer = Json { ignoreUnknownKeys = true }
    companion object {
        fun <T> wrap(type: String, data: T): WebSocketResponseDto<T> {
            return WebSocketResponseDto(type, data)
        }
    }

    val json: String get() = serializer.encodeToString(serializer(), this)
}