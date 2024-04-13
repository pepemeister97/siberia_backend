package siberia.modules.product.data.dto.groups.systemevents

import siberia.conf.AppConf

class ProductGroupUpdateEvent(
    override val author: String,
    override val eventObjectName: String,
    override val eventObjectId: Int,
    override val rollbackInstance: String
) : ProductGroupEvent() {

    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent

    override val eventDescription: String
        get() = "Product` group '$eventObjectName' was updated"
}