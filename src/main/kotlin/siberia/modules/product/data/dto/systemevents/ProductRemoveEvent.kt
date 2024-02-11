package siberia.modules.product.data.dto.systemevents

import siberia.conf.AppConf

data class ProductRemoveEvent(
    override val author: String,
    val removedProductName: String,
    val removedProductVendorCode: String,
    override val eventObjectId: Int,
    override val rollbackInstance: String
) : ProductEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "Product $removedProductName (Vendor Code = $removedProductVendorCode) was removed."
    override val eventObjectName: String
        get() = removedProductName
}