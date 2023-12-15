package siberia.modules.user.data.dto.systemevents.user

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

abstract class UserEvent: SystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.userEvent
}