package siberia.modules.logger.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SystemEventOutputDto (
    val id: Int,
    val author: String,
    val eventType: String,
    val description: String,
    val timestamp: String,
)