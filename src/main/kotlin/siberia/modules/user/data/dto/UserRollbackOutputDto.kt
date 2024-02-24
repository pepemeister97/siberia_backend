package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.rbac.data.dto.LinkedRuleInputDto

@Serializable
data class UserRollbackOutputDto (
    val name: String,
    val login: String,
    val hash: String? = null,
    var rules: List<LinkedRuleInputDto> = listOf(),
    var roles: List<Int> = listOf()
)