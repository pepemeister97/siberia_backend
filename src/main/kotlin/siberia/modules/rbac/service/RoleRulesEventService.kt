package siberia.modules.rbac.service

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.rbac.data.dto.RoleRollbackDto
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.utils.kodein.KodeinEventService

class RoleRulesEventService(di: DI) : KodeinEventService(di) {
    private val rbacService: RbacService by instance()

    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) : Unit = transaction {
        val updateEventData = event.getRollbackData<RoleRollbackDto>()
        with(
            RoleModel.slice(RoleModel.id).select {
                RoleModel.id eq event.eventObjectId
            }.map {
                it[RoleModel.id].value
            }
        ){
            if (this.isNotEmpty()){
                if (updateEventData.objectDto.remove)
                    rbacService.appendRulesToRole(authorizedUser, event.eventObjectId!!, updateEventData.objectDto.rules, needLog = false)
                else
                    rbacService.removeRulesFromRole(authorizedUser, event.eventObjectId!!, updateEventData.objectDto.rules, needLog = false)
            } else {
                throw BadRequestException("rollback failed model removed")
            }
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}