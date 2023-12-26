package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserPatchDto (
    val name: String? = null,
    val login: String? = null,
    val password: String? = null
)