package siberia.modules.product.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

data class ProductMassiveCreateEvent(
    override val author: String, override val rollbackInstance: String
): ResettableSystemEventCreateDto() {
    override val eventObjectId: Int
        get() = 0
    override val eventType: Int
        get() = AppConf.eventTypes.createEvent
    override val eventDescription: String
        get() = "Some products were added"
    override val eventObjectName: String
        get() = "From file upload"

    override val rollbackRoute: String
        get() = "product/bulk"
    override val eventObjectType: Int
        get() = AppConf.objectTypes.productBulkCreate
}