package siberia.modules.rbac.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.rbac.data.dto.RoleRollbackDto
import siberia.utils.kodein.KodeinEventService

class RoleRulesEventService(di: DI) : KodeinEventService(di) {
    private val rbacService: RbacService by instance()

    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val updateEventData = event.getRollbackData<RoleRollbackDto>()
        if (updateEventData.objectDto.remove)
            rbacService.appendRulesToRole(authorizedUser, event.eventObjectId!!, updateEventData.objectDto.rules, needLog = false)
        else
            rbacService.removeRulesFromRole(authorizedUser, event.eventObjectId!!, updateEventData.objectDto.rules, needLog = false)
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}