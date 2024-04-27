package siberia.modules.user.service

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.dto.UserRollbackOutputDto
import siberia.modules.user.data.dto.UserUpdateDto
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinEventService
import siberia.utils.security.bcrypt.CryptoUtil

class UserEventService(di: DI) : KodeinEventService(di) {
    private val userAccessControlService: UserAccessControlService by instance()

    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) : Unit = transaction {
        val updateEventDto = event.getRollbackData<UserUpdateDto>()
        with(
            UserModel.select {
                UserModel.id eq updateEventDto.objectId
            }.map {
                it[UserModel.id]
            }
        ){
            if (this.isNotEmpty()) {
                val authorName = UserDao[authorizedUser.id].login
                val userDao = UserDao[updateEventDto.objectId]
                userDao.loadAndFlush(authorName, updateEventDto.objectDto)
            }
            else throw BadRequestException("rollback failed model removed")
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) : Unit = transaction {
        val createEventDto = event.getRollbackData<UserRollbackOutputDto>()
        val createUserDto = createEventDto.objectDto
        UserDao.checkUnique(createUserDto.login)

        val authorName = UserDao[authorizedUser.id].login

        val userDao = UserDao.new(authorName) {
            name = createUserDto.name
            login = createUserDto.login
            hash = createUserDto.hash ?: CryptoUtil.hash("")
        }

        SystemEventModel.replaceRemovedWithNewId(
            event.eventObjectTypeId,
            createEventDto.objectId,
            userDao.idValue
        )

        SystemEventModel.replaceRemovedWithNewId(
            AppConf.objectTypes.userRightsEvent,
            createEventDto.objectId,
            userDao.idValue
        )

        try {
            userAccessControlService.addRules(userDao, createUserDto.rules)

            userAccessControlService.addRoles(userDao, createUserDto.roles)

        } catch (e: Exception) {
            rollback()
            throw BadRequestException("Bad rules or roles provided")
        }

        commit()
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }

}