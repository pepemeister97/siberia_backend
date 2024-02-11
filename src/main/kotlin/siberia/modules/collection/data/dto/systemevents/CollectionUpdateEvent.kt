package siberia.modules.collection.data.dto.systemevents

import siberia.conf.AppConf

data class CollectionUpdateEvent(
    override val author: String,
    val updatedCollectionName: String,
    override val rollbackInstance: String,
    override val eventObjectId: Int,
) : CollectionEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Collection $updatedCollectionName was updated."
    override val eventObjectName: String
        get() = updatedCollectionName
}