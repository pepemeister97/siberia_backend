package siberia.modules.user.data.dto.systemevents.useraccess.roles

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class UserRolesEvent: ResettableSystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.userRightsEvent

    override val rollbackRoute: String
        get() = "user/roles"
}