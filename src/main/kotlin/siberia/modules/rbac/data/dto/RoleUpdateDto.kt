package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleUpdateDto (
    val name: String? = null,
    val description: String? = null
)