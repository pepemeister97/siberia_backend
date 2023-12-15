package siberia.modules.category.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

abstract class CategoryEvent : SystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.categoryEvent
}