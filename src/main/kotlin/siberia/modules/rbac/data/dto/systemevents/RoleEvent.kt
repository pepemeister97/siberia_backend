package siberia.modules.rbac.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class RoleEvent : ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "rbac/roles"
    override val eventObjectType: Int
        get() = AppConf.objectTypes.roleEvent
}