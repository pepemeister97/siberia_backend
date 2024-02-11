package siberia.modules.user.data.dto

import siberia.modules.rbac.data.dto.LinkedRuleInputDto

data class UserRollbackOutput (
    val name: String,
    val login: String,
    val hash: String? = null,
    var rules: List<LinkedRuleInputDto> = listOf(),
    var roles: List<Int> = listOf()
)