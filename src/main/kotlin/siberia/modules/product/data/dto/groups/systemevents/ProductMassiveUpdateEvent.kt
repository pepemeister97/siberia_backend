package siberia.modules.product.data.dto.groups.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

class ProductMassiveUpdateEvent(
    override val author: String,
    override val eventObjectId: Int,
    override val eventObjectName: String,
    override val rollbackInstance: String
): ResettableSystemEventCreateDto() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent

    override val eventDescription: String
        get() = "Products massive update (by group $eventObjectName)"
    override val rollbackRoute: String
        get() = "product/groups"

    override val eventObjectType: Int
        get() = AppConf.objectTypes.productGroupEvent
}