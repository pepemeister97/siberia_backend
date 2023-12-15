package siberia.modules.brand.data.dto.systemevents

import siberia.conf.AppConf

data class BrandUpdateEvent(
    override val author: String, val updatedBrandName: String
) : BrandEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Brand $updatedBrandName was updated."
    override val eventObjectName: String
        get() = updatedBrandName
}