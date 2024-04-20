package siberia.modules.user.data.dto.systemevents.useraccess.roles

import siberia.conf.AppConf

data class UserRolesRemovedEvent(
    override val author: String,
    override val eventObjectName: String,
    override val eventObjectId: Int,
    override val rollbackInstance: String
): UserRolesEvent() {
    override val eventDescription: String
        get() = "Role was removed"
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
}