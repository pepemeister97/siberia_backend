package siberia.modules.rbac.service

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
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
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) : Unit = transaction {
        val updateEventData = event.getRollbackData<RoleUpdateDto>()
        with(
            RoleModel.slice(RoleModel.id).select {
                RoleModel.id eq updateEventData.objectId
            }.map {
                it[RoleModel.id]
            }
        ){
            if (this.isNotEmpty())
                rbacService.updateRole(authorizedUser, updateEventData.objectId, updateEventData.objectDto)
            else throw BadRequestException("rollback failed model removed")
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) : Unit = transaction {
        val createEventData = event.getRollbackData<RoleRollbackDto>()
        rbacService.createRole(authorizedUser, createEventData.objectDto)

        val userIdList = mutableListOf<Int>()
        createEventData.objectDto.relatedUsers.forEach {
            userIdList.add(it.first)
        }
        UserModel.slice(UserModel.id).select {
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