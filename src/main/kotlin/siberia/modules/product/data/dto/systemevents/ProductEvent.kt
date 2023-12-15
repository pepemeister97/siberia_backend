package siberia.modules.product.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

abstract class ProductEvent: SystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.productEvent
}