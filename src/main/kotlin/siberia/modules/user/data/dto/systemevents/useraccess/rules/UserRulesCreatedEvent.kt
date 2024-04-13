package siberia.modules.user.data.dto.systemevents.useraccess.rules

import siberia.conf.AppConf

data class UserRulesCreatedEvent(
    override val author: String,
    override val eventObjectName: String,
    override val eventDescription: String,
    override val eventObjectId: Int,
    override val rollbackInstance: String
): UserRulesEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
}