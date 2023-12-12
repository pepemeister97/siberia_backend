package siberia.modules.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleOutputDto (
    val id: Int,
    val name: String,
    var rules: List<LinkedRuleOutputDto> = listOf(),
) {}