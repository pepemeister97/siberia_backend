package siberia.utils.websockets.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import siberia.plugins.Logger
import siberia.utils.database.SerializableAny

@Serializable
data class WebSocketResponseDto (
    val type: String,
    val data: String = "",
) {
    companion object {
        @Transient val serializer = Json { ignoreUnknownKeys = true }

        fun wrap(type: String, data: String = ""): WebSocketResponseDto {
            return wrap<String>(type, data)
        }

        @OptIn(InternalSerializationApi::class)
        inline fun <reified T: SerializableAny> wrap(type: String, data: T): WebSocketResponseDto {
            return WebSocketResponseDto(type, serializer.encodeToString(T::class.serializer(), data))
        }
    }

    val json: String get() {
        Logger.debug(this, "main")
        return serializer.encodeToString(serializer(), this)
    }
}