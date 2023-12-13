package siberia.modules.user.service

import io.ktor.util.date.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.dto.*
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService
import siberia.utils.security.bcrypt.CryptoUtil

class UserService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()
    fun createUser(authorizedUser: AuthorizedUser, createUserDto: CreateUserDto): UserOutputDto = transaction {

        UserDao.checkUnique(createUserDto.params.login)

        val userDao = UserDao.new {
            name = createUserDto.params.name
            login = createUserDto.params.login
            hash = CryptoUtil.hash(createUserDto.params.password)
            lastLogin = getTimeMillis()
        }

        try {
            userAccessControlService.addRules(userDao, createUserDto.rules)

            userAccessControlService.addRoles(userDao, createUserDto.roles)

        } catch (e: Exception) {
            rollback()
            throw BadRequestException("Bad rules or roles provided")
        }

        commit()

        UserOutputDto(
            id = userDao.idValue,
            name = userDao.name,
            login = userDao.login,
            hash = userDao.hash,
            lastLogin = 0,
        )
    }

    fun removeUser(authorizedUser: AuthorizedUser, userId: Int): UserRemoveOutputDto = transaction {
        val userDao = UserDao[userId]
        userDao.delete()

        UserRemoveOutputDto(userId, "success")
    }

    fun updateUser(authorizedUser: AuthorizedUser, userId: Int, userPatchDto: UserPatchDto): UserOutputDto = transaction {
        val userDao = UserDao[userId]
        userDao.loadPatch(userPatchDto)
        userDao.flush()
        userDao.toOutputDto()
    }
}