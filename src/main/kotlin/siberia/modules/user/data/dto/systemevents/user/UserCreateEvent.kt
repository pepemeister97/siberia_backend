package siberia.modules.user.data.dto.systemevents.user

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

data class UserCreateEvent(
    val authorName: String, val createdUserLogin: String
) : SystemEventCreateDto(

) {
    override val author: String
        get() = authorName
    override val eventType: Int
        get() = AppConf.eventTypes.createEvent
    override val eventDescription: String
        get() = "User $createdUserLogin was created."
    override val eventObjectName: String
        get() = createdUserLogin
    override val eventObjectType: Int
        get() = AppConf.objectTypes.userEvent
}