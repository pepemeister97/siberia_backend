package siberia.modules.rbac.service

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.ValidateException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.auth.service.AuthSocketService
import siberia.modules.logger.data.dto.SystemEventCreateDto
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.rbac.data.dao.RoleDao
import siberia.modules.rbac.data.dao.RuleCategoryDao
import siberia.modules.rbac.data.dao.RuleDao
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.rbac.data.dto.*
import siberia.modules.rbac.data.dto.systemevents.RoleCreateEvent
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.rbac.data.models.rule.RuleModel
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.transaction.data.dao.TransactionStatusDao.Companion.createLikeCond
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class RbacService(di: DI) : KodeinService(di) {
    private val authSocketService: AuthSocketService by instance()
    private fun logEvent(event: SystemEventCreateDto) {
        SystemEventModel.logEvent(event)
    }
    private fun logUpdateEvent(author: AuthorizedUser, roleDao: RoleDao, linkedRules: List<LinkedRuleInputDto>) {
        val authorDao = UserDao[author.id]
        SystemEventModel.logResettableEvent(roleDao.getRulesPatchEvent(authorDao.name, linkedRules))
    }

    private fun logRemoveEvent(author: AuthorizedUser, roleDao: RoleDao, linkedRules: List<LinkedRuleInputDto>) {
        val authorDao = UserDao[author.id]
        SystemEventModel.logResettableEvent(roleDao.getRulesPatchEvent(authorDao.name, linkedRules, remove = true))
    }

//    fun getAllRoles(): List<RoleOutputDto> = transaction { RoleDao.find { Op.nullOp() }.map { it.toOutputDto() } }

    fun getFiltered(roleFilterDto: RoleFilterDto): List<RoleOutputDto> = transaction {
        RoleDao.find {
            createLikeCond(roleFilterDto.name, RoleModel.id neq 0, RoleModel.name)
        }.map { it.withRelatedUsers }
    }

    fun getRole(roleId: Int): RoleOutputDto = transaction { RoleDao[roleId].outputWithChildren }

    fun getAllRules(): List<RuleOutputDto> = transaction {
        RuleDao.wrapRows(RuleModel.selectAll()).map { it.toOutputDto() }
    }


    private fun List<LinkedRuleOutputDto>.appendToRole(roleDao: RoleDao): List<LinkedRuleOutputDto> = transaction {
        map { link ->
            RbacModel.insert {
                it[role] = roleDao.idValue
                it[rule] = link.ruleId
                it[stock] = link.stockId
            }
            link
        }
    }

    fun validateRule(ruleId: Int, stockId: Int? = null): LinkedRuleOutputDto = transaction {
        val ruleDao = RuleDao[ruleId]
        if (ruleDao.needStock) {
            if (stockId != null)
                StockDao[stockId]
            else
                throw ValidateException.build {
                    addError(ValidateException.ValidateError("stock_id", "must be provided"))
                }
        }
        LinkedRuleOutputDto(ruleId = ruleId, stockId = stockId)
    }

    fun validateRole(roleId: Int): RoleOutputDto = transaction {
        val roleDao = RoleDao[roleId]
        RoleOutputDto(
            roleDao.idValue, roleDao.name, roleDao.description,
            roleDao.outputWithChildren.rules.map {
                validateRule(it.ruleId, it.stockId)
            }
        )
    }

    fun createRole(authorizedUser: AuthorizedUser, roleCreateDto: RoleInputDto): RoleOutputDto = transaction {
        val roleDao = RoleDao.new {
            name = roleCreateDto.name
            description = roleCreateDto.description
        }

        val linkedRules = if (roleCreateDto.rules.isNotEmpty())
            appendRulesToRole(authorizedUser, roleDao.idValue, roleCreateDto.rules, false)
        else listOf()

        val author = UserDao[authorizedUser.id]
        logEvent(RoleCreateEvent(roleDao.idValue, roleDao.name, author.login))
        commit()

        RoleOutputDto(roleDao.idValue, roleDao.name, roleDao.description, linkedRules)
    }

    fun appendRulesToRole(authorizedUser: AuthorizedUser, roleId: Int, linkedRules: List<LinkedRuleInputDto>, needLog: Boolean = true): List<LinkedRuleOutputDto> = transaction {
        val roleDao = RoleDao[roleId]
        val appendedRules = linkedRules.map {
            validateRule(it.ruleId, it.stockId)
        }.appendToRole(roleDao).run {
            if (needLog)
                logUpdateEvent(authorizedUser, roleDao, linkedRules)
            this
        }
        RbacModel.expandAppendedRules(roleId, appendedRules)
        commit()

        val relatedUsers = RbacModel.getRelatedUsers(roleId).map { it[UserModel.id].value }.filter { it != authorizedUser.id }
        authSocketService.updateRules(relatedUsers)

        appendedRules
    }

    fun removeRulesFromRole(authorizedUser: AuthorizedUser, roleId: Int, linkedRules: List<LinkedRuleInputDto>, needLog: Boolean = true) = transaction {
        val roleDao = RoleDao[roleId]

        RbacModel.unlinkRules(RbacModel.role eq roleDao.idValue, linkedRules)
        if (needLog)
            logRemoveEvent(authorizedUser, roleDao, linkedRules)
        RbacModel.removeExpandedRules(roleId, linkedRules)
        commit()

        val relatedUsers = RbacModel.getRelatedUsers(roleId).map { it[UserModel.id].value }.filter { it != authorizedUser.id }
        authSocketService.updateRules(relatedUsers)
    }

    fun updateRole(authorizedUser: AuthorizedUser, roleId: Int, roleUpdateDto: RoleUpdateDto): RoleOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val roleDao = RoleDao[roleId]
        roleDao.loadAndFlush(userDao.login, roleUpdateDto)

        commit()

        roleDao.toOutputDto()
    }

    fun getAllCategories(): List<RuleCategoryOutputDto> = RuleCategoryDao.find { Op.nullOp() }.map { it.toOutputDto() }

    fun removeRole(authorizedUser: AuthorizedUser, roleId: Int): RoleRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val roleDao = RoleDao[roleId]
        val roleName = roleDao.name
        roleDao.delete(userDao.login)
        commit()

        RoleRemoveResultDto(true, "Role $roleName successfully removed")
    }
}