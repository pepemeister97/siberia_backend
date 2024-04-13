package siberia.modules.user.data.dto.systemevents.useraccess.rules

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class UserRulesEvent: ResettableSystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.userRightsEvent

    override val rollbackRoute: String
        get() = "rbac/user/rules"
}