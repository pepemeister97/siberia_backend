package siberia.modules.auth.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.models.rule.UserToRuleModel
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass

class UserToRuleDao(id: EntityID<Int>) : BaseIntEntity<LinkedRuleOutputDto>(id, UserToRuleModel) {

    companion object: BaseIntEntityClass<LinkedRuleOutputDto, UserToRuleDao>(UserToRuleModel)

    private val _userId by UserToRuleModel.user
    val userId
        get() = _userId.value
    var user by UserDao referencedOn UserToRuleModel.user

    private val _ruleId by UserToRuleModel.rule
    val ruleId
        get() = _ruleId.value
    var rule by RuleDao referencedOn UserToRuleModel.rule

    private val _stockId by UserToRuleModel.stock
    val stockId
        get() = _stockId?.value
    var stock by StockDao optionalReferencedOn UserToRuleModel.stock

    override fun toOutputDto(): LinkedRuleOutputDto
        = LinkedRuleOutputDto(
            ruleId = ruleId,
            stockId = stockId
        )
}