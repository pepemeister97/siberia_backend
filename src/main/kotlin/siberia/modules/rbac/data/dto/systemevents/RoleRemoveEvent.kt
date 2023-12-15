package siberia.modules.rbac.data.dto.systemevents

import siberia.conf.AppConf

class RoleRemoveEvent (
        override val author: String, private val removedRoleName: String
) : RoleEvent() {
        override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
        override val eventDescription: String
        get() = "Role $removedRoleName was removed."
        override val eventObjectName: String
        get() = removedRoleName
    }