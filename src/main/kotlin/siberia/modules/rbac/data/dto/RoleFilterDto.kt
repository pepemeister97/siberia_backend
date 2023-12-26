package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleFilterDto (
    val name: String? = null
)