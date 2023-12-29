package siberia.modules.logger.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SystemEventSearchFilter (
    val author: String? = null,
    val rangeStart: Long? = null,
    val rangeEnd: Long? = null,
    val eventTypeId: List<Int>? = null,
    val eventObjectTypeId: List<Int>? = null
)