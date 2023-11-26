package siberia.modules.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LinkedRuleOutputDto (
    val ruleId: Int,
    val stockId: Int? = null,
)