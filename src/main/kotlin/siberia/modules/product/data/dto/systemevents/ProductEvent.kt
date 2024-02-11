package siberia.modules.product.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class ProductEvent: ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "product"
    override val eventObjectType: Int
        get() = AppConf.objectTypes.productEvent
}