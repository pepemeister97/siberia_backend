package siberia.modules.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenOutputDto (
    val accessToken: String,
    val refreshToken: String
)