package siberia.modules.user.data.dto.systemevents.useraccess

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

data class UserRightsUpdated(
    override val author: String,
    override val eventObjectName: String,
    override val eventDescription: String,

    override val eventType: Int = AppConf.eventTypes.updateEvent,
    override val eventObjectType: Int = AppConf.objectTypes.userEvent
): SystemEventCreateDto() {
}