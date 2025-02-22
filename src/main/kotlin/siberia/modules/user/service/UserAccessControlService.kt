package siberia.modules.user.service

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.modules.rbac.data.dto.LinkedRuleInputDto
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.rbac.service.RbacService
import siberia.modules.user.data.dao.UserDao
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.auth.service.AuthSocketService
import siberia.modules.user.data.dto.systemevents.useraccess.roles.UserRolesCreatedEvent
import siberia.modules.user.data.dto.systemevents.useraccess.roles.UserRolesRemovedEvent
import siberia.modules.user.data.dto.systemevents.useraccess.rules.UserRulesCreatedEvent
import siberia.modules.user.data.dto.systemevents.useraccess.rules.UserRulesRemovedEvent
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class UserAccessControlService(di: DI) : KodeinService(di) {
    private val authSocketService: AuthSocketService by instance()
    private val rbacService: RbacService by instance()

    private enum class UpdateDirection {
        CREATED, REMOVED
    }

    private fun logUpdateRoles(author: AuthorizedUser, target: UserDao, updateDirection: UpdateDirection, old: List<RoleOutputDto>) = transaction {
        val description = "User roles were updated"
        val authorName: String = UserDao[author.id].login
        val eventInstance = BaseIntEntity.EventInstance(
            getUserRoles(target.idValue),
            old
        )
        val rollbackInstance = eventInstance.serialize()
        val event = if (updateDirection == UpdateDirection.CREATED)
            UserRolesCreatedEvent(authorName, target.login, description, target.idValue, rollbackInstance)
        else
            UserRolesRemovedEvent(authorName, target.login, description, target.idValue, rollbackInstance)

        SystemEventModel.logResettableEvent(event)
    }

    private fun logUpdateRules(author: AuthorizedUser, target: UserDao, updateDirection: UpdateDirection, old: List<LinkedRuleOutputDto>) = transaction {
        val description = "User rules were updated"
        val authorName: String = UserDao[author.id].login
        val eventInstance = BaseIntEntity.EventInstance(
            getUserRules(target.idValue),
            old
        )
        val rollbackInstance = eventInstance.serialize()
        val event = if (updateDirection == UpdateDirection.CREATED)
            UserRulesCreatedEvent(authorName, target.login, description, target.idValue, rollbackInstance)
        else
            UserRulesRemovedEvent(authorName, target.login, description, target.idValue, rollbackInstance)

        SystemEventModel.logResettableEvent(event)
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
        logUpdateRules(authorizedUser, userDao, UpdateDirection.CREATED, getUserRules(targetId))
        val addedRules = addRules(userDao, newRules)
        if (userDao.idValue != authorizedUser.id)
            authSocketService.updateRules(userDao.idValue)
        addedRules
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
        logUpdateRoles(authorizedUser, userDao, UpdateDirection.CREATED, getUserRoles(targetId))
        val addedRoles = addRoles(userDao, newRoles)
        if (userDao.idValue != authorizedUser.id)
            authSocketService.updateRules(userDao.idValue)
        addedRoles
    }

    fun getUserRules(authorizedUser: AuthorizedUser): List<LinkedRuleOutputDto> = transaction { UserDao[authorizedUser.id].rulesWithStocks }

    fun getUserRules(userId: Int): List<LinkedRuleOutputDto> = transaction { UserDao[userId].rulesWithStocks }

    fun getUserRoles(authorizedUser: AuthorizedUser): List<RoleOutputDto> = transaction { UserDao[authorizedUser.id].rolesWithRules }

    fun getUserRoles(userId: Int): List<RoleOutputDto> = transaction { UserDao[userId].rolesWithRules }

    fun removeRules(authorizedUser: AuthorizedUser, targetId: Int, linkedRules: List<LinkedRuleInputDto>, shadowed: Boolean = false) = transaction {
        val targetDao = UserDao[targetId]
        if (!shadowed)
            logUpdateRules(authorizedUser, targetDao, UpdateDirection.REMOVED, getUserRules(targetId))
        RbacModel.unlinkRules((RbacModel.user eq targetDao.idValue) and RbacModel.simplifiedBy.isNull(), linkedRules)
        commit()
        if (authorizedUser.id != targetDao.idValue)
            authSocketService.updateRules(targetDao.idValue)
    }

    fun removeRoles(authorizedUser: AuthorizedUser, targetId: Int, linkedRoles: List<Int>, shadowed: Boolean = false) = transaction {
        val targetDao = UserDao[targetId]
        if (!shadowed)
            logUpdateRoles(authorizedUser, targetDao, UpdateDirection.REMOVED, getUserRoles(targetId))
        RbacModel.unlinkRoles(targetDao.idValue, linkedRoles)
        commit()
        if (authorizedUser.id != targetDao.idValue)
            authSocketService.updateRules(targetDao.idValue)
    }

    fun checkAccessToStock(userId: Int, ruleId: Int, stockId: Int): Boolean = transaction {
        RbacModel.select {
            (RbacModel.user eq userId) and (RbacModel.rule eq ruleId) and (RbacModel.stock eq stockId)
        }.count() > 0
    }

    fun checkAccessToStock(userId: Int, stockId: Int): Boolean = transaction {
        RbacModel.select {
            (RbacModel.user eq userId) and (RbacModel.stock eq stockId)
        }.count() > 0
    }

    // Return Map <StockID, List<Rules>>
    private fun translateRbacModelsToStockRulesMap(models: Query): Map<Int, List<Int>> = transaction {
        val result = mutableMapOf<Int, MutableList<Int>>()
        models.forEach {
            val ruleId = it[RbacModel.rule]!!.value
            val stockId = it[RbacModel.stock]!!.value
            if (result[stockId] != null)
                result[stockId]!!.add(ruleId)
            else
                result[stockId] = mutableListOf(ruleId)
        }
        result
    }

    // Return Map <StockID, List<Rules>>
    // Returns stocks which can be used by user in operations
    fun getAvailableStocksByOperations(userId: Int): Map<Int, List<Int>> = transaction {
        val models = RbacModel.select {
            (RbacModel.user eq userId) and
            (RbacModel.stock.isNotNull()) and
            (RbacModel.rule.isNotNull()) and
            (RbacModel.rule notInList listOf(
                AppConf.rules.concreteStockView
            ))
        }
        translateRbacModelsToStockRulesMap(models)
    }

    fun getAvailableStocks(userId: Int): Map<Int, List<Int>> = transaction {
        val models = RbacModel.select {
            (RbacModel.user eq userId) and
            (RbacModel.stock.isNotNull()) and
            (RbacModel.rule.isNotNull())
        }
        translateRbacModelsToStockRulesMap(models)
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