package siberia.modules.product.data.dto.groups.systemevents

import siberia.conf.AppConf

class ProductGroupRemoveEvent(
    override val author: String,
    override val eventObjectName: String,
    override val eventObjectId: Int,
    override val rollbackInstance: String
) : ProductGroupEvent() {

    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent

    override val eventDescription: String
        get() = "Product` $eventObjectName group was removed"
}