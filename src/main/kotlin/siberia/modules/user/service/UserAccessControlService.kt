package siberia.modules.user.service

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.rbac.data.dto.LinkedRuleInputDto
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.rbac.service.RbacService
import siberia.modules.user.data.dao.UserDao
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.user.data.dto.systemevents.useraccess.UserRightsUpdated
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class UserAccessControlService(di: DI) : KodeinService(di) {
    private val rbacService: RbacService by instance()

    private fun logUpdate(author: AuthorizedUser, target: String, description: String) = transaction {
        val authorName: String = UserDao[author.id].login
        val event = UserRightsUpdated(authorName, target, description)
        SystemEventModel.logEvent(event)
    }

    private fun List<LinkedRuleOutputDto>.appendToUser(userDao: UserDao, simplifiedBy: Int? = null): List<LinkedRuleOutputDto> =
        map { link ->
            RbacModel.insert {
                it[user] = userDao.idValue
                it[rule] = link.ruleId
                it[stock] = link.stockId
                it[RbacModel.simplifiedBy] = simplifiedBy
            }
            link
        }

    private fun List<RoleOutputDto>.appendToUser(userDao: UserDao): List<RoleOutputDto> =
        map { roleDto ->
            val relation = RbacModel.insert {
                it[user] = userDao.idValue
                it[role] = roleDto.id
            }
            roleDto.rules.appendToUser(
                userDao, simplifiedBy = relation.resultedValues!!.first()[RbacModel.id].value
            )
            roleDto
        }

    fun addRules(userDao: UserDao, newRules: List<LinkedRuleInputDto>): List<LinkedRuleOutputDto> = transaction {
        newRules.map {
            val linkedRule = rbacService.validateRule(it.ruleId, it.stockId)
            linkedRule
        }.appendToUser(userDao)
    }

    fun addRules(authorizedUser: AuthorizedUser, targetId: Int, newRules: List<LinkedRuleInputDto>): List<LinkedRuleOutputDto> = transaction {
        val userDao = UserDao[targetId]
        logUpdate(authorizedUser, userDao.login, "New rules added")
        addRules(userDao, newRules)
    }

    fun addRoles(userDao: UserDao, newRoles: List<Int>): List<RoleOutputDto> = transaction {
        newRoles.map {
            rbacService.validateRole(it)
        }.appendToUser(userDao)
    }

    fun addRoles(authorizedUser: AuthorizedUser, targetId: Int, newRoles: List<Int>): List<RoleOutputDto> = transaction {
        val userDao = UserDao[targetId]
        logUpdate(authorizedUser, userDao.login, "New roles added")
        addRoles(userDao, newRoles)
    }

    fun getUserRules(authorizedUser: AuthorizedUser): List<LinkedRuleOutputDto> = transaction { UserDao[authorizedUser.id].rulesWithStocks }

    fun getUserRoles(authorizedUser: AuthorizedUser): List<RoleOutputDto> = transaction { UserDao[authorizedUser.id].rolesWithRules }

    fun removeRules(authorizedUser: AuthorizedUser, targetId: Int, linkedRules: List<LinkedRuleInputDto>) {
        val targetDao = UserDao[targetId]
        RbacModel.unlinkRules(RbacModel.user eq targetDao.idValue, linkedRules)
        logUpdate(authorizedUser, targetDao.login, "Some rules were removed")
    }

    fun removeRoles(authorizedUser: AuthorizedUser, targetId: Int, linkedRoles: List<Int>) {
        val targetDao = UserDao[targetId]
        RbacModel.unlinkRoles(targetDao.idValue, linkedRoles)
        logUpdate(authorizedUser, targetDao.login, "Some roles were removed")
    }
}