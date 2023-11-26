package siberia.modules.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LinkedRuleInputDto (
    val ruleId: Int,
    val stockId: Int? = null,
)