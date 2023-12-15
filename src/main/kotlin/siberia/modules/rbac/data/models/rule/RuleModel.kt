package siberia.modules.rbac.data.models.rule

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.utils.database.BaseIntIdTable

object RuleModel : BaseIntIdTable() {
    val name = text("name")
    val category = reference("category", RuleCategoryModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable().default(null)
    val needStock = bool("need_stock")
}