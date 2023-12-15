package siberia.modules.logger.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.logger.data.dto.`object`.ObjectAfterDto
import siberia.modules.logger.data.dto.`object`.ObjectBeforeDto

@Serializable
data class SystemEventOutputDto <T> (
    val id: Int,
    val author: String,
    val eventType: String,
    val eventObjectType: String,
    val eventObjectName: String,
    val eventDescription: String,

    //Next iterations
    val eventObject: Int? = null,
    val objectBefore: ObjectBeforeDto<T>? = null,
    val objectAfter: ObjectAfterDto<T>? = null,
    val timestamp: String,
)