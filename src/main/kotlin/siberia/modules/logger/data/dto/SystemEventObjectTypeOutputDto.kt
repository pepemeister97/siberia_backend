package siberia.modules.logger.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SystemEventObjectTypeOutputDto (
    val id: Int,
    val name: String
)