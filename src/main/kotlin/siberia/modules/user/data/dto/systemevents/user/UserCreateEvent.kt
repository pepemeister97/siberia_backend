package siberia.modules.user.data.dto.systemevents.user

import siberia.conf.AppConf

data class UserCreateEvent(
    override val author: String, val createdUserLogin: String
) : UserEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.createEvent
    override val eventDescription: String
        get() = "User $createdUserLogin was created."
    override val eventObjectName: String
        get() = createdUserLogin
}