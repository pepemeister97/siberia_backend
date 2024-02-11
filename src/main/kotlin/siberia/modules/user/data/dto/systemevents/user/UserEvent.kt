package siberia.modules.user.data.dto.systemevents.user

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class UserEvent: ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "user"
    override val eventObjectType: Int
        get() = AppConf.objectTypes.userEvent
}