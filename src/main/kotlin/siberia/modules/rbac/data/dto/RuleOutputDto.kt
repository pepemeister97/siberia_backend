package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RuleOutputDto (
    val id: Int,
    val name: String,
    val needStock: Boolean,
    val category: RuleCategoryOutputDto? = null,
)