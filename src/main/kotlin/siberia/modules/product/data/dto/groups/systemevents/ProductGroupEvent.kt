package siberia.modules.product.data.dto.groups.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class ProductGroupEvent: ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "product/groups"

    override val eventObjectType: Int
        get() = AppConf.objectTypes.productGroupEvent
}