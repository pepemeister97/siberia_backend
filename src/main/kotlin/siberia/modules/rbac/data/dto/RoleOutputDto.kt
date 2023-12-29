package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleOutputDto (
    val id: Int,
    val name: String,
    val description: String?,
    var rules: List<LinkedRuleOutputDto> = listOf(),
    val relatedUsersCount: Long? = null,
    val relatedUsers: List<String>? = null
) {}