package siberia.modules.brand.data.dto.systemevents

import siberia.conf.AppConf

data class BrandCreateEvent(
    override val author: String,
    val createdBrandName: String,
    override val eventObjectId: Int
) : BrandEvent() {
    override val rollbackInstance: String
        get() = ""
    override val eventType: Int
        get() = AppConf.eventTypes.createEvent
    override val eventDescription: String
        get() = "Brand $createdBrandName was created."
    override val eventObjectName: String
        get() = createdBrandName
}