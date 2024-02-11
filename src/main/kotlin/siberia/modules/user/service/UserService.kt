package siberia.modules.user.service

import io.ktor.util.date.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.transaction.data.dao.TransactionStatusDao.Companion.createLikeCond
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.dto.*
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService
import siberia.utils.security.bcrypt.CryptoUtil

class UserService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()

    fun createUser(authorizedUser: AuthorizedUser, createUserDto: CreateUserDto): UserOutputDto = transaction {

        UserDao.checkUnique(createUserDto.params.login)

        val authorName = UserDao[authorizedUser.id].login

        val userDao = UserDao.new(authorName) {
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
            lastLogin = 0,
        )
    }

    fun removeUser(authorizedUser: AuthorizedUser, userId: Int): UserRemoveOutputDto = transaction {
        val authorName = UserDao[authorizedUser.id].login
        val userDao = UserDao[userId]

        userDao.delete(authorName)
        commit()

        UserRemoveOutputDto(userId, "success")
    }

    fun updateUser(authorizedUser: AuthorizedUser, userId: Int, userUpdateDto: UserUpdateDto): UserOutputDto = transaction {
        val authorName = UserDao[authorizedUser.id].login
        val userDao = UserDao[userId]

        if (userUpdateDto.hash != null)
            throw BadRequestException("Bad request")

        userDao.loadAndFlush(authorName, userUpdateDto)
        commit()

        userDao.toOutputDto()
    }

    fun getOne(userId: Int): UserOutputDto = transaction { UserDao[userId].toOutputDto() }

    fun getByFilter(userFilterDto: UserFilterDto): List<UserOutputDto> = transaction {
        UserDao.find {
            createLikeCond(userFilterDto.login, UserModel.id neq 0, UserModel.login) and
            createLikeCond(userFilterDto.name, UserModel.id neq 0, UserModel.name)
        }.map { it.toOutputDto() }
    }
}