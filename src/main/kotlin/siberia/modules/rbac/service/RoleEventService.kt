package siberia.modules.rbac.service

import org.jetbrains.exposed.sql.select
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.rbac.data.dto.RoleRollbackDto
import siberia.modules.rbac.data.dto.RoleUpdateDto
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.user.data.models.UserModel
import siberia.modules.user.service.UserAccessControlService
import siberia.utils.kodein.KodeinEventService

class RoleEventService(di: DI) : KodeinEventService(di) {
    private val rbacService: RbacService by instance()
    private val userAccessControlService: UserAccessControlService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val updateEventData = event.getRollbackData<RoleUpdateDto>()
        with(
            RoleModel.select {
                RoleModel.id eq updateEventData.objectId
            }.map {
                it[RoleModel.id]
            }
        ){
            if (this.isNotEmpty())
                rbacService.updateRole(authorizedUser, updateEventData.objectId, updateEventData.objectDto)
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val createEventData = event.getRollbackData<RoleRollbackDto>()
        rbacService.createRole(authorizedUser, createEventData.objectDto)

        val userIdList = mutableListOf<Int>()
        createEventData.objectDto.relatedUsers.forEach {
            userIdList.add(it.first)
        }
        UserModel.select {
            UserModel.id inList userIdList
        }.map {
            it[UserModel.id].value
        }.forEach {
            userAccessControlService.addRoles(authorizedUser, it, listOf(createEventData.objectId))
        }
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}