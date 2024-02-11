package siberia.modules.collection.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class CollectionEvent : ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "collection"
    override val eventObjectType: Int
        get() = AppConf.objectTypes.collectionEvent
}