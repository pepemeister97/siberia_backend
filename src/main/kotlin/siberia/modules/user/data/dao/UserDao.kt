package siberia.modules.user.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.exceptions.BadRequestException
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.conf.AppConf
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.rbac.data.dto.LinkedRuleInputDto
import siberia.modules.user.data.dto.UserOutputDto
import siberia.modules.user.data.dto.UserRollbackOutputDto
import siberia.modules.user.data.dto.UserUpdateDto
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
                throw BadRequestException("Login must be unique")
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

    val hasAccessToProcessTransfers: Boolean
        get() = rulesWithStocks.any { it.ruleId == AppConf.rules.manageTransferRequest }

    override fun toOutputDto(): UserOutputDto =
        UserOutputDto(idValue, name, login, null, lastLogin)

    private fun toOutputWithHash(): UserOutputDto =
        UserOutputDto(idValue, name, login, hash, lastLogin)

    private fun outputForRollback(): UserRollbackOutputDto {
        val rules = RbacModel.userToRuleLinks(idValue, withStock = true, expanded = false).map { LinkedRuleInputDto(it.ruleId, it.stockId) }
        val roles = RbacModel.userToRoleLinks(idValue, withRules = false).map { it.id }
        return UserRollbackOutputDto(name, login, hash, rules, roles)
    }

    private fun loadPatch(userUpdateDto: UserUpdateDto) = transaction {
        if (userUpdateDto.login != null && userUpdateDto.login != login) {
            checkUnique(userUpdateDto.login!!)
            login = userUpdateDto.login!!
        }
        if (userUpdateDto.name != null)
            name = userUpdateDto.name!!
        if (userUpdateDto.password != null)
            hash = CryptoUtil.hash(userUpdateDto.password!!)
        if (userUpdateDto.hash != null)
            hash = CryptoUtil.hash(userUpdateDto.hash!!)
    }

    fun loadAndFlush(authorName: String, userUpdateDto: UserUpdateDto): Boolean {
        val event = UserUpdateEvent(
            authorName,
            login,
            userUpdateDto.login ?: login,
            idValue,
            createRollbackUpdateDto<UserOutputDto, UserUpdateDto>(userUpdateDto, toOutputWithHash())
        )
        SystemEventModel.logResettableEvent(event)

        loadPatch(userUpdateDto)

        return flush()
    }

    fun delete(authorName: String) {
        val event = UserRemoveEvent(
            authorName,
            login,
            idValue,
            createRollbackRemoveDto(outputForRollback())
        )
        SystemEventModel.logResettableEvent(event)

        super.delete()
    }
}