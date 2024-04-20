package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleRollbackDto (
    override val name: String,
    override val description: String? = null,
    override val rules: List<LinkedRuleInputDto> = listOf(),
    val relatedUsers: List<Pair<Int, String>> = listOf(),
    val remove: Boolean = false
): RoleInputDto()