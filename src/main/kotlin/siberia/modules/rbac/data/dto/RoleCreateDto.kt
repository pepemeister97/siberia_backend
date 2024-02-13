package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleCreateDto (
    override val name: String,
    override val description: String? = null,
    override val rules: List<LinkedRuleInputDto> = listOf()
): RoleInputDto()