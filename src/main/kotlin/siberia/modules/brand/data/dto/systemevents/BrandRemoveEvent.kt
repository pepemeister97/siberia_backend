package siberia.modules.brand.data.dto.systemevents

import siberia.conf.AppConf

data class BrandRemoveEvent(
    override val author: String, val removedBrandName: String
) : BrandEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "Brand $removedBrandName was removed."
    override val eventObjectName: String
        get() = removedBrandName
}