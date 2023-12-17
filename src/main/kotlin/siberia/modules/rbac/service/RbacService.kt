package siberia.modules.rbac.service

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.exceptions.ValidateException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventCreateDto
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.rbac.data.dao.RoleDao
import siberia.modules.rbac.data.dao.RuleCategoryDao
import siberia.modules.rbac.data.dao.RuleDao
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.rbac.data.dto.*
import siberia.modules.rbac.data.dto.systemevents.RoleCreateEvent
import siberia.modules.rbac.data.dto.systemevents.RoleRemoveEvent
import siberia.modules.rbac.data.dto.systemevents.RoleUpdateEvent
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class RbacService(di: DI) : KodeinService(di) {
    private fun logEvent(event: SystemEventCreateDto) {
        SystemEventModel.logEvent(event)
    }
    private fun logUpdateEvent(author: AuthorizedUser, target: String) {
        val authorDao = UserDao[author.id]
        logEvent(RoleUpdateEvent(authorDao.login, target))
    }

    fun getAllRoles(): List<RoleOutputDto> = transaction { RoleDao.find { Op.nullOp() }.map { it.toOutputDto() } }

    fun getRole(roleId: Int): RoleOutputDto = transaction { RoleDao[roleId].outputWithChildren }

    fun getAllRules(): List<RuleOutputDto> =
        RuleDao.find { Op.nullOp() }.map { it.toOutputDto() }

    private fun List<LinkedRuleOutputDto>.appendToRole(roleDao: RoleDao): List<LinkedRuleOutputDto> =
        map { link ->
            RbacModel.insert {
                it[role] = roleDao.idValue
                it[rule] = link.ruleId
                it[stock] = link.stockId
            }
            link
        }

    fun validateRule(ruleId: Int, stockId: Int? = null): LinkedRuleOutputDto {
        val ruleDao = RuleDao[ruleId]
        if (ruleDao.needStock) {
            if (stockId != null)
                StockDao[stockId]
            else
                throw ValidateException.build {
                    addError(ValidateException.ValidateError("stock_id", "must be provided"))
                }
        }
        return LinkedRuleOutputDto(ruleId, stockId)
    }

    fun validateRole(roleId: Int): RoleOutputDto {
        val roleDao = RoleDao[roleId]
        return RoleOutputDto(
            roleDao.idValue, roleDao.name, roleDao.description,
            roleDao.outputWithChildren.rules.map {
                validateRule(it.ruleId, it.stockId)
            }
        )
    }

    fun createRole(authorizedUser: AuthorizedUser, roleInputDto: RoleInputDto): RoleOutputDto = transaction {
        val roleDao = RoleDao.new {
            name = roleInputDto.name
        }

        val linkedRules = if (roleInputDto.linkedRuleInputDto.isNotEmpty())
            appendRulesToRole(authorizedUser, roleDao.idValue, roleInputDto.linkedRuleInputDto, false)
        else listOf()

        val author = UserDao[authorizedUser.id]
        logEvent(RoleCreateEvent(author.login, roleDao.name))

        RoleOutputDto(roleDao.idValue, roleDao.name, roleDao.description, linkedRules)
    }

    fun appendRulesToRole(authorizedUser: AuthorizedUser, roleId: Int, linkedRules: List<LinkedRuleInputDto>, needLog: Boolean = true): List<LinkedRuleOutputDto> {
        val roleDao = RoleDao[roleId]
        return linkedRules.map {
            validateRule(it.ruleId, it.stockId)
        }.appendToRole(roleDao).run {
            if (needLog)
                logUpdateEvent(authorizedUser, roleDao.name)
            this
        }
    }

    fun removeRulesFromRole(authorizedUser: AuthorizedUser, roleId: Int, linkedRules: List<LinkedRuleInputDto>) = transaction {
        val roleDao = RoleDao[roleId]

        RbacModel.unlinkRules(RbacModel.role eq roleDao.idValue, linkedRules)
        logUpdateEvent(authorizedUser, roleDao.name)
    }

    fun updateRole(authorizedUser: AuthorizedUser, roleId: Int, roleInputDto: RoleInputDto): RoleOutputDto = transaction {
        val roleDao = RoleDao[roleId]

        roleDao.name = roleInputDto.name
        roleDao.flush()

        logUpdateEvent(authorizedUser, roleDao.name)

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
        RoleRemoveResultDto(true, "Role $roleName successfully removed")
    }
}