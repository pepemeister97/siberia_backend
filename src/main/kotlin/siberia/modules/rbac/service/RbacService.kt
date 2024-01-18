package siberia.modules.rbac.service

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import siberia.utils.database.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.ValidateException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventCreateDto
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.notifications.service.NotificationService
import siberia.modules.rbac.data.dao.RoleDao
import siberia.modules.rbac.data.dao.RuleCategoryDao
import siberia.modules.rbac.data.dao.RuleDao
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.rbac.data.dto.*
import siberia.modules.rbac.data.dto.systemevents.RoleCreateEvent
import siberia.modules.rbac.data.dto.systemevents.RoleRemoveEvent
import siberia.modules.rbac.data.dto.systemevents.RoleUpdateEvent
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.rbac.data.models.rule.RuleModel
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.transaction.data.dao.TransactionStatusDao.Companion.createLikeCond
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class RbacService(di: DI) : KodeinService(di) {
    private val notificationService: NotificationService by instance()
    private fun logEvent(event: SystemEventCreateDto) {
        SystemEventModel.logEvent(event)
    }
    private fun logUpdateEvent(author: AuthorizedUser, oldTarget: String, target: String) {
        val authorDao = UserDao[author.id]
        logEvent(RoleUpdateEvent(authorDao.login, oldTarget, target))
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

    fun createRole(authorizedUser: AuthorizedUser, roleInputDto: RoleInputDto): RoleOutputDto = transaction {
        val roleDao = RoleDao.new {
            name = roleInputDto.name
            description = roleInputDto.description
        }

        val linkedRules = if (roleInputDto.rules.isNotEmpty())
            appendRulesToRole(authorizedUser, roleDao.idValue, roleInputDto.rules, false)
        else listOf()

        val author = UserDao[authorizedUser.id]
        logEvent(RoleCreateEvent(author.login, roleDao.name))
        commit()

        RoleOutputDto(roleDao.idValue, roleDao.name, roleDao.description, linkedRules)
    }

    fun appendRulesToRole(authorizedUser: AuthorizedUser, roleId: Int, linkedRules: List<LinkedRuleInputDto>, needLog: Boolean = true): List<LinkedRuleOutputDto> = transaction {
        val roleDao = RoleDao[roleId]
        val appendedRules = linkedRules.map {
            validateRule(it.ruleId, it.stockId)
        }.appendToRole(roleDao).run {
            if (needLog)
                logUpdateEvent(authorizedUser, roleDao.name, roleDao.name)
            this
        }
        RbacModel.expandAppendedRules(roleId, appendedRules)
        commit()

        val relatedUsers = RbacModel.getRelatedUsers(roleId).map { it[UserModel.id].value }
        notificationService.emitUpdateRules(relatedUsers)

        appendedRules
    }

    fun removeRulesFromRole(authorizedUser: AuthorizedUser, roleId: Int, linkedRules: List<LinkedRuleInputDto>) = transaction {
        val roleDao = RoleDao[roleId]

        RbacModel.unlinkRules(RbacModel.role eq roleDao.idValue, linkedRules)
        logUpdateEvent(authorizedUser, roleDao.name, roleDao.name)
        RbacModel.removeExpandedRules(roleId, linkedRules)
        commit()

        val relatedUsers = RbacModel.getRelatedUsers(roleId).map { it[UserModel.id].value }
        notificationService.emitUpdateRules(relatedUsers)
    }

    fun updateRole(authorizedUser: AuthorizedUser, roleId: Int, roleUpdateDto: RoleUpdateDto): RoleOutputDto = transaction {
        val roleDao = RoleDao[roleId]
        val oldName = roleDao.name
        roleDao.loadUpdateDto(roleUpdateDto)

        roleDao.flush()

        logUpdateEvent(authorizedUser, oldName, roleDao.name)
        commit()

        roleDao.toOutputDto()
    }

    fun getAllCategories(): List<RuleCategoryOutputDto> = RuleCategoryDao.find { Op.nullOp() }.map { it.toOutputDto() }

    fun removeRole(authorizedUser: AuthorizedUser, roleId: Int): RoleRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val roleDao = RoleDao[roleId]
        val roleName = roleDao.name
        val event = RoleRemoveEvent(userDao.login, roleName)
        roleDao.delete()
        SystemEventModel.logEvent(event)
        commit()

        RoleRemoveResultDto(true, "Role $roleName successfully removed")
    }
}