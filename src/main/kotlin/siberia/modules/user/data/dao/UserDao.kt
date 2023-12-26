package siberia.modules.user.data.dao

import org.jetbrains.exposed.dao.EntityBatchUpdate
import org.jetbrains.exposed.dao.id.EntityID
import siberia.utils.database.transaction
import siberia.exceptions.UnauthorizedException
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.user.data.dto.UserOutputDto
import siberia.modules.user.data.dto.UserPatchDto
import siberia.modules.user.data.dto.systemevents.user.UserCreateEvent
import siberia.modules.user.data.dto.systemevents.user.UserRemoveEvent
import siberia.modules.user.data.dto.systemevents.user.UserUpdateEvent
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue
import siberia.utils.security.bcrypt.CryptoUtil

class UserDao(id: EntityID<Int>): BaseIntEntity<UserOutputDto>(id, UserModel) {
    companion object : BaseIntEntityClass<UserOutputDto, UserDao>(UserModel) {
        fun checkUnique(login: String) = transaction {
            val search = UserDao.find {
                UserModel.login eq login
            }
            if (!search.empty())
                throw UnauthorizedException()
        }

        fun new(authorName: String, init: UserDao.() -> Unit): UserDao {
            val userDao = super.new(init)
            val userCreateEvent = UserCreateEvent(authorName, userDao.login)
            SystemEventModel.logEvent(userCreateEvent)
            return userDao
        }
    }

    var name by UserModel.name
    var login by UserModel.login
    var hash by UserModel.hash
    var lastLogin by UserModel.lastLogin

    val rolesWithRules: List<RoleOutputDto>
        get() = RbacModel.userToRoleLinks(idValue, withRules = true, withStock = true)

    val rulesWithStocks: List<LinkedRuleOutputDto>
        get() = RbacModel.userToRuleLinks(userId = idValue, withStock = true)

    override fun toOutputDto(): UserOutputDto =
        UserOutputDto(idValue, name, login, null, lastLogin)

    fun loadPatch(userPatchDto: UserPatchDto) = transaction {
        if (userPatchDto.login != null) {
            checkUnique(userPatchDto.login)
            login = userPatchDto.login
        }
        if (userPatchDto.name != null)
            name = userPatchDto.name
        if (userPatchDto.password != null)
            hash = CryptoUtil.hash(userPatchDto.password)
    }

    fun flush(authorName: String, oldLogin: String, batch: EntityBatchUpdate? = null): Boolean {
        val userUpdateEvent = UserUpdateEvent(authorName, oldLogin, login)
        SystemEventModel.logEvent(userUpdateEvent)
        return super.flush(batch)
    }

    fun delete(authorName: String) {
        val userRemoveEvent = UserRemoveEvent(authorName, login)
        SystemEventModel.logEvent(userRemoveEvent)
        super.delete()
    }
}