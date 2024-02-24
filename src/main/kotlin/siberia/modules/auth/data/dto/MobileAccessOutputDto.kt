package siberia.modules.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MobileAccessOutputDto (
    val token: String,
    val type: String
)