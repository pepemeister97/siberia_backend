package siberia.modules.product.data.dto.groups.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

class ProductGroupCreateEvent(
    override val author: String,
    override val eventObjectName: String,
    override val eventObjectId: Int,
) : SystemEventCreateDto() {

    override val eventType: Int
        get() = AppConf.eventTypes.createEvent

    override val eventDescription: String
        get() = "Product` group '$eventObjectName' was created"

    override val eventObjectType: Int
        get() = AppConf.objectTypes.productGroupEvent
}