package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserOutputDto (
    val id: Int,
    val name: String,
    val login: String,
    val hash: String? = null,
    val lastLogin: Long
)