package siberia.modules.user.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.user.data.dto.systemevents.useraccess.UserRolesRollbackDto
import siberia.utils.kodein.KodeinEventService

class UserRolesEventService(di: DI) : KodeinEventService(di) {
    private val userAccessControlService: UserAccessControlService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto): Unit = transaction {
        val updateEventDto = event.getRollbackData<UserRolesRollbackDto>()

        userAccessControlService.addRoles(
            authorizedUser,
            updateEventDto.objectId,
            updateEventDto.objectDto.roles,
            shadowed = true
        )
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val updateEventDto = event.getRollbackData<UserRolesRollbackDto>()

        userAccessControlService.removeRoles(
            authorizedUser,
            updateEventDto.objectId,
            updateEventDto.objectDto.roles,
            shadowed = true
        )
    }
}