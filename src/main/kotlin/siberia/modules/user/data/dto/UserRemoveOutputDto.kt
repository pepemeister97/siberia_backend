package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserRemoveOutputDto (
    val userId: Int,
    val message: String
)