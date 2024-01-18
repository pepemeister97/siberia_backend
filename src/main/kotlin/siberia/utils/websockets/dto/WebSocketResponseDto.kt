package siberia.utils.websockets.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import siberia.plugins.Logger

@Serializable
data class WebSocketResponseDto (
    val type: String,
) {
    @Transient private val serializer = Json { ignoreUnknownKeys = true }
    companion object {
        fun wrap(type: String): WebSocketResponseDto {
            return WebSocketResponseDto(type)
        }
    }

    val json: String get() {
        Logger.debug(this, "main")
        return serializer.encodeToString(serializer(), this)
    }
}