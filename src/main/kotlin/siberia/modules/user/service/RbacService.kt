package siberia.modules.user.service

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.exceptions.ValidateException
import siberia.modules.auth.data.dao.RoleDao
import siberia.modules.auth.data.dao.RuleCategoryDao
import siberia.modules.auth.data.dao.RuleDao
import siberia.modules.auth.data.dto.*
import siberia.modules.auth.data.models.role.RbacModel
import siberia.modules.stock.data.dao.StockDao
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class RbacService(di: DI) : KodeinService(di) {

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
            roleDao.idValue, roleDao.name,
            roleDao.outputWithChildren.rules.map {
                validateRule(it.ruleId, it.stockId)
            }
        )
    }

    fun createRole(roleInputDto: RoleInputDto): RoleOutputDto = transaction {
        val roleDao = RoleDao.new {
            name = roleInputDto.name
        }
        val linkedRules = if (roleInputDto.linkedRuleInputDto.isNotEmpty())
            appendRulesToRole(roleDao.idValue, roleInputDto.linkedRuleInputDto)
        else listOf()

        RoleOutputDto(roleDao.idValue, roleDao.name, linkedRules)
    }

    fun appendRulesToRole(roleId: Int, linkedRules: List<LinkedRuleInputDto>): List<LinkedRuleOutputDto> {
        val roleDao = RoleDao[roleId]
        return linkedRules.map {
            validateRule(it.ruleId, it.stockId)
        }.appendToRole(roleDao)
    }

    fun removeRulesFromRole(roleId: Int, linkedRules: List<LinkedRuleInputDto>) = transaction {
        val roleDao = RoleDao[roleId]

        RbacModel.unlinkRules(RbacModel.role eq roleDao.idValue, linkedRules)
    }

    fun updateRole(roleId: Int, roleInputDto: RoleInputDto): RoleOutputDto = transaction {
        val roleDao = RoleDao[roleId]

        roleDao.name = roleInputDto.name
        roleDao.flush()

        roleDao.toOutputDto()
    }

    fun getAllCategories(): List<RuleCategoryOutputDto> = RuleCategoryDao.find { Op.nullOp() }.map { it.toOutputDto() }
}