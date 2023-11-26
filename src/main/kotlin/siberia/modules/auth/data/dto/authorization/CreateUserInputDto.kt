package siberia.modules.auth.data.dto.authorization

import kotlinx.serialization.Serializable
import siberia.modules.auth.data.dto.LinkedRuleInputDto

@Serializable
data class CreateUserInputDto (
    val login: String,
    val password: String,
    val rules: List<LinkedRuleInputDto> = listOf(),
    val roles: List<Int> = listOf()
)