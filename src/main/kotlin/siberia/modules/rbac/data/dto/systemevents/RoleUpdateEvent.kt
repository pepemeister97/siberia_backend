package siberia.modules.rbac.data.dto.systemevents

import siberia.conf.AppConf

class RoleUpdateEvent (
        override val author: String,
        private val oldTargetName: String,
        private val updatedRoleName: String,
        override val rollbackInstance: String = "",
        override val eventObjectId: Int = 0,
) : RoleEvent() {
        override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
        override val eventDescription: String
        get() = "Role $oldTargetName was updated."
        override val eventObjectName: String
        get() = updatedRoleName
    }