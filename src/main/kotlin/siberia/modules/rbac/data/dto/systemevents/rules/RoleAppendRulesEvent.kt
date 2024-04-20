package siberia.modules.rbac.data.dto.systemevents.rules

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto


class RoleAppendRulesEvent(
    override val author: String,
    override val eventObjectId: Int,
    override val eventObjectName: String,
    override val rollbackInstance: String
): ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "rbac/roles/rules"

    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Rules were added to role '$eventObjectName'"

    override val eventObjectType: Int
        get() = AppConf.objectTypes.roleEvent
}