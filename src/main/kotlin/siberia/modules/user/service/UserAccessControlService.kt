package siberia.modules.user.service

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.LinkedRuleInputDto
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.dto.RoleOutputDto
import siberia.modules.auth.data.models.role.RbacModel
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.dto.AuthorizedUser
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class UserAccessControlService(di: DI) : KodeinService(di) {
    private val rbacService: RbacService by instance()

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

    fun addRules(authorizedUser: AuthorizedUser, newRules: List<LinkedRuleInputDto>): List<LinkedRuleOutputDto> = transaction {
        val userDao = UserDao[authorizedUser.id]
        addRules(userDao, newRules)
    }

    fun addRoles(userDao: UserDao, newRoles: List<Int>): List<RoleOutputDto> = transaction {
        newRoles.map {
            rbacService.validateRole(it)
        }.appendToUser(userDao)
    }

    fun addRoles(authorizedUser: AuthorizedUser, newRoles: List<Int>): List<RoleOutputDto> = transaction {
        val userDao = UserDao[authorizedUser.id]
        addRoles(userDao, newRoles)
    }

    fun getUserRules(authorizedUser: AuthorizedUser): List<LinkedRuleOutputDto> = transaction { UserDao[authorizedUser.id].rulesWithStocks }

    fun getUserRoles(authorizedUser: AuthorizedUser): List<RoleOutputDto> = transaction { UserDao[authorizedUser.id].rolesWithRules }

    fun removeRules(authorizedUser: AuthorizedUser, linkedRules: List<LinkedRuleInputDto>) {
        RbacModel.unlinkRules(RbacModel.user eq authorizedUser.id, linkedRules)
    }

    fun removeRoles(authorizedUser: AuthorizedUser, linkedRoles: List<Int>) {
        RbacModel.unlinkRoles(authorizedUser.id, linkedRoles)
    }
}