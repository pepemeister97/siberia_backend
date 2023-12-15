package siberia.modules.user.data.dto

import siberia.modules.rbac.data.dto.LinkedRuleInputDto

data class CreateUserDto (
    val params: UserInputDto,
    val roles: List<Int>,
    val rules: List<LinkedRuleInputDto>
)