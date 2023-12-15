package siberia.modules.product.data.dto.systemevents

import siberia.conf.AppConf

data class ProductCreateEvent(
    override val author: String, val createdProductName: String, val createdProductVendorCode: String
) : ProductEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.createEvent
    override val eventDescription: String
        get() = "Product $createdProductName ($createdProductVendorCode) was created."
    override val eventObjectName: String
        get() = createdProductName
}