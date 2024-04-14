package siberia.modules.user.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.auth.service.AuthSocketService
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.dto.systemevents.useraccess.UserRulesRollbackDto
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinEventService

class UserRulesEventService(di: DI) : KodeinEventService(di) {
    private val userAccessControlService: UserAccessControlService by instance()
    private val authSocketService: AuthSocketService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto): Unit = transaction {
        val updateEventDto = event.getRollbackData<UserRulesRollbackDto>()
        val userDao = UserDao[updateEventDto.objectId]
        if (userDao.idValue != authorizedUser.id)
            authSocketService.updateRules(userDao.idValue)
        userAccessControlService.addRules(userDao, updateEventDto.objectDto.rules)
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val updateEventDto = event.getRollbackData<UserRulesRollbackDto>()
        val userDao = UserDao[updateEventDto.objectId]

        if (userDao.idValue != authorizedUser.id)
            authSocketService.updateRules(userDao.idValue)

        userAccessControlService.removeRules(
            authorizedUser,
            updateEventDto.objectId,
            updateEventDto.objectDto.rules,
            shadowed = true
        )
    }
}