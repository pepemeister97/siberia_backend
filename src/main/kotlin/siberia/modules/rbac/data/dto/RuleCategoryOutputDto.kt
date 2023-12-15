package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RuleCategoryOutputDto (
    val id: Int,
    val name: String
)