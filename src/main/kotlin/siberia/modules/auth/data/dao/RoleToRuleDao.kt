package siberia.modules.auth.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.models.role.RoleToRuleModel
import siberia.modules.stock.data.dao.StockDao
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass

class RoleToRuleDao(id: EntityID<Int>): BaseIntEntity<LinkedRuleOutputDto>(id, RoleToRuleModel) {

    companion object: BaseIntEntityClass<LinkedRuleOutputDto, RoleToRuleDao>(RoleToRuleModel)

    private val _roleId by RoleToRuleModel.role
    val userId
        get() = _roleId.value
    var user by RoleDao referencedOn RoleToRuleModel.role

    private val _ruleId by RoleToRuleModel.rule
    val ruleId
        get() = _ruleId.value
    var rule by RuleDao referencedOn RoleToRuleModel.rule

    private val _stockId by RoleToRuleModel.stock
    val stockId
        get() = _stockId?.value
    var stock by StockDao optionalReferencedOn RoleToRuleModel.stock

    override fun toOutputDto(): LinkedRuleOutputDto
        = LinkedRuleOutputDto(
            ruleId = ruleId,
            stockId = stockId
        )
}