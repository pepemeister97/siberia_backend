package siberia.modules.user.data.dto

data class UserPatchDto (
    val name: String? = null,
    val login: String? = null,
    val password: String? = null
)