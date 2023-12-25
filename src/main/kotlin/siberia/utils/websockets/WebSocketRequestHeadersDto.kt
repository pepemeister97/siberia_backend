package siberia.utils.websockets

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketRequestHeadersDto (
    val authorization: String,
    val uri: String
)