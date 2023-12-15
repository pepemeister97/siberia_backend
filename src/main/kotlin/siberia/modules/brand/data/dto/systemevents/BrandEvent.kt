package siberia.modules.brand.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

abstract class BrandEvent : SystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.brandEvent
}