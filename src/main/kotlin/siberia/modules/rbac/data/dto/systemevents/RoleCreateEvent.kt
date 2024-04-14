package siberia.modules.rbac.data.dto.systemevents

import siberia.conf.AppConf

class RoleCreateEvent (
        override val eventObjectId: Int,
        override val author: String,
        private val createdRoleName: String
) : RoleEvent() {

        override val rollbackInstance: String
                get() = ""
        override val eventType: Int
        get() = AppConf.eventTypes.createEvent
        override val eventDescription: String
        get() = "Role $createdRoleName was created."
        override val eventObjectName: String
        get() = createdRoleName
    }