package siberia.modules.user.data.dto.systemevents.user

import siberia.conf.AppConf

data class UserUpdateEvent(
    override val author: String, val oldLogin: String, val updatedUserLogin: String
) : UserEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "User $oldLogin was updated."
    override val eventObjectName: String
        get() = updatedUserLogin
}