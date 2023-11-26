package siberia.modules.auth.data.dto.authorization

import kotlinx.serialization.Serializable

@Serializable
data class TokenOutputDto (
    val accessToken: String,
    val refreshToken: String
)