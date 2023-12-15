package siberia.modules.rbac.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

class RoleUpdateEvent (
    private val authorName: String, private val updatedRoleName: String
) : SystemEventCreateDto() {
        override val author: String
        get() = authorName
        override val eventType: Int
        get() = AppConf.eventTypes.createEvent
        override val eventDescription: String
        get() = "Role $updatedRoleName was updated."
        override val eventObjectName: String
        get() = updatedRoleName
        override val eventObjectType: Int
        get() = AppConf.objectTypes.userEvent
    }