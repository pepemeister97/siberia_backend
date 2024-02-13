package siberia.modules.rbac.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.rbac.data.dto.RoleRollbackDto
import siberia.modules.rbac.data.dto.RoleUpdateDto
import siberia.modules.user.service.UserAccessControlService
import siberia.utils.kodein.KodeinEventService

class RoleEventService(di: DI) : KodeinEventService(di) {
    private val rbacService: RbacService by instance()
    private val userAccessControlService: UserAccessControlService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val updateEventData = event.getRollbackData<RoleUpdateDto>()
        rbacService.updateRole(authorizedUser, updateEventData.objectId, updateEventData.objectDto)
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val createEventData = event.getRollbackData<RoleRollbackDto>()
        rbacService.createRole(authorizedUser, createEventData.objectDto)
        createEventData.objectDto.relatedUsers.forEach {
            userAccessControlService.addRoles(authorizedUser, it.first, listOf(createEventData.objectId))
        }
    }
}