package siberia.modules.brand.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class BrandEvent : ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "brand"
    override val eventObjectType: Int
        get() = AppConf.objectTypes.brandEvent
}