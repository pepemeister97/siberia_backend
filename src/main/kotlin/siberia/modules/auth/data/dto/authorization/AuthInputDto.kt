package siberia.modules.auth.data.dto.authorization

import kotlinx.serialization.Serializable

@Serializable
data class AuthInputDto (
    val login: String,
    val password: String
)