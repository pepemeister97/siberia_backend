package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleInputDto (
    val name: String,
    val description: String? = null,
    val rules: List<LinkedRuleInputDto> = listOf()
)