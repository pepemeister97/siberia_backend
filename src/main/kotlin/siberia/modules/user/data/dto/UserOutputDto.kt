package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto

@Serializable
data class UserOutputDto (
    val id: Int,
    val name: String,
    val login: String,
    val hash: String? = null,
    val lastLogin: Long,
    var rules: List<LinkedRuleOutputDto> = listOf()
)