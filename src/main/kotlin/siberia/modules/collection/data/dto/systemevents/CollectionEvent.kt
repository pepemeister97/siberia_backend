package siberia.modules.collection.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

abstract class CollectionEvent : SystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.collectionEvent
}