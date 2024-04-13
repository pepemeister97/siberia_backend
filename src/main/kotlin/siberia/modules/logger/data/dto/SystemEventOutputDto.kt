package siberia.modules.logger.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.EMPTY
import siberia.utils.database.SerializableAny

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
    val eventObjectId: Int? = null,
    val canBeReset: Boolean,
    var rollbackInstance: String? = null,
    var rollbackRoute: String? = null,

    //Next iterations
//    val eventObject: Int? = null,
//    val objectBefore: ObjectBeforeDto<T>? = null,
//    val objectAfter: ObjectAfterDto<T>? = null,
    val timestamp: String,
) {

    data class UpdateEventDto <T> (
        val objectId: Int,
        val objectDto: T
    )

    @Transient val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T : SerializableAny> getRollbackData(): UpdateEventDto<T> {
        val objectId = eventObjectId ?: throw Exception("Bad event")
        if (rollbackInstance == null || rollbackInstance == "")
            throw Exception("Rollback instance is not provided")
        val eventInstance = json.decodeFromString<BaseIntEntity.EventInstance<T, EMPTY>>(rollbackInstance!!)
        return UpdateEventDto(objectId, eventInstance.rollbackInstance)
    }
}