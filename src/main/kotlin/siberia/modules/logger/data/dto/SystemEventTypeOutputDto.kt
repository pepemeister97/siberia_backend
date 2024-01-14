package siberia.modules.logger.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SystemEventTypeOutputDto (
    val id: Int,
    val name: String
)