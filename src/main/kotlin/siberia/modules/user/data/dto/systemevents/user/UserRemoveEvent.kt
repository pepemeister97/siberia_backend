package siberia.modules.user.data.dto.systemevents.user

import siberia.conf.AppConf

data class UserRemoveEvent(
    override val author: String,
    val removedUserLogin: String,
    override val eventObjectId: Int,
    override val rollbackInstance: String
) : UserEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "User $removedUserLogin was removed."
    override val eventObjectName: String
        get() = removedUserLogin
}