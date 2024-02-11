package siberia.modules.category.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class CategoryEvent : ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "category"
    override val eventObjectType: Int
        get() = AppConf.objectTypes.categoryEvent
}