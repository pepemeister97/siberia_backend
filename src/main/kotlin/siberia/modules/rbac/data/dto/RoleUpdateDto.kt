package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleUpdateDto (
    var name: String? = null,
    var description: String? = null
)