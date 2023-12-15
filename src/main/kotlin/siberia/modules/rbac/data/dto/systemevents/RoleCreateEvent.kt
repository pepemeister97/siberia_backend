package siberia.modules.rbac.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

class RoleCreateEvent (
    private val authorName: String, private val createdRoleName: String
) : SystemEventCreateDto() {
        override val author: String
        get() = authorName
        override val eventType: Int
        get() = AppConf.eventTypes.createEvent
        override val eventDescription: String
        get() = "Role $createdRoleName was created."
        override val eventObjectName: String
        get() = createdRoleName
        override val eventObjectType: Int
        get() = AppConf.objectTypes.userEvent
    }