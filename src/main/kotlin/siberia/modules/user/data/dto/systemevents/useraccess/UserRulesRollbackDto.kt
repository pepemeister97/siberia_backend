package siberia.modules.user.data.dto.systemevents.useraccess

import kotlinx.serialization.Serializable
import siberia.modules.rbac.data.dto.LinkedRuleInputDto

@Serializable
data class UserRulesRollbackDto (
    val rules: List<LinkedRuleInputDto>
)