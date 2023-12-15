package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LinkedRuleOutputDto (
    val ruleId: Int,
    val categoryId: Int? = null,
    val needStock: Boolean? = null,
    val stockId: Int? = null,
    var stockName: String? = null
)