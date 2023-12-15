package siberia.modules.user.data.dto.systemevents.user

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

data class UserRemoveEvent(
    val authorName: String, val removedUserLogin: String
) : SystemEventCreateDto(

) {
    override val author: String
        get() = authorName
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "User $removedUserLogin was removed."
    override val eventObjectName: String
        get() = removedUserLogin
    override val eventObjectType: Int
        get() = AppConf.objectTypes.userEvent
}