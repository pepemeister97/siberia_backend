package siberia.modules.user.data.dto.systemevents.useraccess

import kotlinx.serialization.Serializable

@Serializable
data class UserRolesRollbackDto (
    val roles: List<Int>
)