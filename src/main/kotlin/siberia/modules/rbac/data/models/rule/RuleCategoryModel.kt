package siberia.modules.rbac.data.models.rule

import siberia.utils.database.BaseIntIdTable

object RuleCategoryModel : BaseIntIdTable() {
    val name = text("name")
}