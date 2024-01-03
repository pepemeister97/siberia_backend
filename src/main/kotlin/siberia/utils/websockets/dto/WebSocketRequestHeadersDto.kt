package siberia.utils.websockets.dto

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketRequestHeadersDto (
    val authorization: String,
    val uri: String
)