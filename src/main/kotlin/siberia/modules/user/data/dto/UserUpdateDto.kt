package siberia.modules.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateDto (
    var name: String? = null,
    var login: String? = null,
    var password: String? = null,
    var hash: String? = null
)