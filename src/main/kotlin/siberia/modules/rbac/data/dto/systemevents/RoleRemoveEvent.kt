package siberia.modules.rbac.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

class RoleRemoveEvent (
    private val authorName: String, private val removedRoleName: String
) : SystemEventCreateDto() {
        override val author: String
        get() = authorName
        override val eventType: Int
        get() = AppConf.eventTypes.createEvent
        override val eventDescription: String
        get() = "Role $removedRoleName was removed."
        override val eventObjectName: String
        get() = removedRoleName
        override val eventObjectType: Int
        get() = AppConf.objectTypes.userEvent
    }