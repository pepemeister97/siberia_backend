package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.rbac.data.dto.LinkedRuleInputDto

@Serializable
data class CreateUserDto (
    val params: UserInputDto,
    val roles: List<Int>,
    val rules: List<LinkedRuleInputDto>
)