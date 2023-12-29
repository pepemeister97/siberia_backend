package siberia.modules.logger.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SystemEventOutputDto (
    val id: Int,
    val author: String,
    val eventTypeId: Int,
    val eventType: String,
    val eventObjectType: String,
    val eventObjectTypeId: Int,
    val eventObjectName: String,
    val eventDescription: String,

    //Next iterations
//    val eventObject: Int? = null,
//    val objectBefore: ObjectBeforeDto<T>? = null,
//    val objectAfter: ObjectAfterDto<T>? = null,
    val timestamp: String,
)