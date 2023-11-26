package siberia.modules.auth.data.dto.authorization

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenDto (
    val id: Int,
    val lastLogin: Long
)