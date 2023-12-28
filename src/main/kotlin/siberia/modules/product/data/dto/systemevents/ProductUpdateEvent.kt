package siberia.modules.product.data.dto.systemevents

import siberia.conf.AppConf

data class ProductUpdateEvent(
    override val author: String, val updatedProductName: String, val updatedProductVendorCode: String
) : ProductEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Product $updatedProductName ($updatedProductVendorCode) was updated."
    override val eventObjectName: String
        get() = updatedProductName
}