package siberia.modules.user.data.dto.systemevents.useraccess.roles

import siberia.conf.AppConf

data class UserRolesCreatedEvent(
    override val author: String,
    override val eventObjectName: String,
    override val eventDescription: String,
    override val eventObjectId: Int,
    override val rollbackInstance: String
): UserRolesEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.createEvent
}