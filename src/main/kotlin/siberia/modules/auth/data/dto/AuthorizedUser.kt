package siberia.modules.auth.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto

@Serializable
data class AuthorizedUser (
    val id: Int,
    val rules: List<LinkedRuleOutputDto>,
    //For mobile access
    val stockId: Int? = null,
    val transactionId: Int? = null
)