package siberia.modules.collection.data.dto.systemevents

import siberia.conf.AppConf

data class CollectionRemoveEvent(
    override val author: String, val removedCollectionName: String
) : CollectionEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "Collection $removedCollectionName was removed."
    override val eventObjectName: String
        get() = removedCollectionName
}