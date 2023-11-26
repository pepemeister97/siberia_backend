package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserOutputDto (
    val id: Int,
    val login: String,
    val hash: String,
    val lastLogin: Long
)