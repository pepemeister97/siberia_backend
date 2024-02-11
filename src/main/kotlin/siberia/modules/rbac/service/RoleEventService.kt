package siberia.modules.rbac.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.rbac.data.dto.RoleInputDto
import siberia.modules.rbac.data.dto.RoleUpdateDto
import siberia.utils.kodein.KodeinEventService

class RoleEventService(di: DI) : KodeinEventService(di) {
    private val rbacService: RbacService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val updateEventData = event.getRollbackData<RoleUpdateDto>()
        rbacService.updateRole(authorizedUser, updateEventData.objectId, updateEventData.objectDto)
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val createEventData = event.getRollbackData<RoleInputDto>()
        rbacService.createRole(authorizedUser, createEventData.objectDto)
    }
}