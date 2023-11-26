package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.auth.data.dto.LinkedRuleOutputDto

@Serializable
data class AuthorizedUser (
    val id: Int,
    val rules: List<LinkedRuleOutputDto>
)