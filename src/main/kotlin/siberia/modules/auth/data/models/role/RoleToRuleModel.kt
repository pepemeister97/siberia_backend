package siberia.modules.auth.data.models.role

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.auth.data.models.rule.RuleModel
import siberia.modules.stock.data.models.StockModel
import siberia.utils.database.BaseIntIdTable

object RoleToRuleModel: BaseIntIdTable() {
    val role = reference("role", RoleModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val rule = reference("rule", RuleModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val stock = reference("stock", StockModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
}