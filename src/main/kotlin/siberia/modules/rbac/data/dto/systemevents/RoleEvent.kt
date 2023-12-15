package siberia.modules.rbac.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

abstract class RoleEvent : SystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.roleEvent
}