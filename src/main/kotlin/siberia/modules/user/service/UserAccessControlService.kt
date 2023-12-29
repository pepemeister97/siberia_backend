package siberia.modules.user.service

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import siberia.utils.database.transaction
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
        val appendedRules = newRules.map {
            val linkedRule = rbacService.validateRule(it.ruleId, it.stockId)
            linkedRule
        }.appendToUser(userDao)
        commit()

        appendedRules
    }

    fun addRules(authorizedUser: AuthorizedUser, targetId: Int, newRules: List<LinkedRuleInputDto>): List<LinkedRuleOutputDto> = transaction {
        val userDao = UserDao[targetId]
        logUpdate(authorizedUser, userDao.login, "New rules added")
        addRules(userDao, newRules)
    }

    fun addRoles(userDao: UserDao, newRoles: List<Int>): List<RoleOutputDto> = transaction {
        val appendedRoles = newRoles.map {
            rbacService.validateRole(it)
        }.appendToUser(userDao)
        commit()

        appendedRoles
    }

    fun addRoles(authorizedUser: AuthorizedUser, targetId: Int, newRoles: List<Int>): List<RoleOutputDto> = transaction {
        val userDao = UserDao[targetId]
        logUpdate(authorizedUser, userDao.login, "New roles added")
        addRoles(userDao, newRoles)
    }

    fun getUserRules(authorizedUser: AuthorizedUser): List<LinkedRuleOutputDto> = transaction { UserDao[authorizedUser.id].rulesWithStocks }

    fun getUserRules(userId: Int): List<LinkedRuleOutputDto> = transaction { UserDao[userId].rulesWithStocks }

    fun getUserRoles(authorizedUser: AuthorizedUser): List<RoleOutputDto> = transaction { UserDao[authorizedUser.id].rolesWithRules }

    fun getUserRoles(userId: Int): List<RoleOutputDto> = transaction { UserDao[userId].rolesWithRules }

    fun removeRules(authorizedUser: AuthorizedUser, targetId: Int, linkedRules: List<LinkedRuleInputDto>) = transaction {
        val targetDao = UserDao[targetId]
        RbacModel.unlinkRules(RbacModel.user eq targetDao.idValue, linkedRules)
        logUpdate(authorizedUser, targetDao.login, "Some rules were removed")
        commit()
    }

    fun removeRoles(authorizedUser: AuthorizedUser, targetId: Int, linkedRoles: List<Int>) = transaction {
        val targetDao = UserDao[targetId]
        RbacModel.unlinkRoles(targetDao.idValue, linkedRoles)
        logUpdate(authorizedUser, targetDao.login, "Some roles were removed")
        commit()
    }

    fun checkAccessToStock(userId: Int, ruleId: Int, stockId: Int): Boolean = transaction {
        RbacModel.select {
            (RbacModel.user eq userId) and (RbacModel.rule eq ruleId) and (RbacModel.stock eq stockId)
        }.count() > 0
    }

    // Return Map <StockID, List<Rules>>
    fun getAvailableStocks(userId: Int): Map<Int, List<Int>> = transaction {
        val result = mutableMapOf<Int, MutableList<Int>>()
        RbacModel.select {
            (RbacModel.user eq userId) and (RbacModel.stock.isNotNull()) and (RbacModel.rule.isNotNull())
        }.forEach {
            val ruleId = it[RbacModel.rule]!!.value
            val stockId = it[RbacModel.stock]!!.value
            if (result[stockId] != null)
                result[stockId]!!.add(ruleId)
            else
                result[stockId] = mutableListOf(ruleId)
        }
        result
    }

    fun filterAvailable(userId: Int, stocks: List<Int>): List<Int> = transaction {
        RbacModel.select {
            (RbacModel.user eq userId) and (RbacModel.stock inList stocks)
        }.mapNotNull { it[RbacModel.stock]?.value }
    }

//    fun getAvailableStocksByRule(userId: Int, ruleId: Int): List<Int> = transaction {
//        RbacModel.select {
//            (RbacModel.user eq userId) and (RbacModel.rule eq ruleId)
//        }.mapNotNull { it[RbacModel.stock]?.value }
//    }
}