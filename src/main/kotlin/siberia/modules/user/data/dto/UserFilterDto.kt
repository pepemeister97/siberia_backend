package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserFilterDto (
    val name: String? = null,
    val login: String? = null
)