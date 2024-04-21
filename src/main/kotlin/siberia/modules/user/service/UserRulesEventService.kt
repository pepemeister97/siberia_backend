package siberia.modules.user.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.user.data.dto.systemevents.useraccess.UserRulesRollbackDto
import siberia.utils.kodein.KodeinEventService

class UserRulesEventService(di: DI) : KodeinEventService(di) {
    private val userAccessControlService: UserAccessControlService by instance()

    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto): Unit = transaction {
        val updateEventDto = event.getRollbackData<UserRulesRollbackDto>()

        userAccessControlService.addRules(
            authorizedUser,
            updateEventDto.objectId,
            updateEventDto.objectDto.rules,
            shadowed = true
        )
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val updateEventDto = event.getRollbackData<UserRulesRollbackDto>()

        userAccessControlService.removeRules(
            authorizedUser,
            updateEventDto.objectId,
            updateEventDto.objectDto.rules,
            shadowed = true
        )
    }
}