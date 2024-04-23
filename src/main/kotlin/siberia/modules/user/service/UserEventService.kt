package siberia.modules.user.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.dto.UserRollbackOutputDto
import siberia.modules.user.data.dto.UserUpdateDto
import siberia.utils.kodein.KodeinEventService
import siberia.utils.security.bcrypt.CryptoUtil

class UserEventService(di: DI) : KodeinEventService(di) {
    private val userAccessControlService: UserAccessControlService by instance()

    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val updateEventDto = event.getRollbackData<UserUpdateDto>()
        val authorName = UserDao[authorizedUser.id].login
        val userDao = UserDao[updateEventDto.objectId]

        userDao.loadAndFlush(authorName, updateEventDto.objectDto)

        return@transaction
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val createEventDto = event.getRollbackData<UserRollbackOutputDto>()
        val createUserDto = createEventDto.objectDto
        UserDao.checkUnique(createUserDto.login)

        val authorName = UserDao[authorizedUser.id].login

        val userDao = UserDao.new(authorName) {
            name = createUserDto.name
            login = createUserDto.login
            hash = createUserDto.hash ?: CryptoUtil.hash("")
        }

        try {
            userAccessControlService.addRules(userDao, createUserDto.rules)

            userAccessControlService.addRoles(userDao, createUserDto.roles)

            return@transaction
        } catch (e: Exception) {
            rollback()
            throw BadRequestException("Bad rules or roles provided")
        }
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }

}