package siberia.modules.user.data.models

import siberia.utils.database.BaseIntIdTable

object UserModel: BaseIntIdTable() {
    val login = text("login")
    val hash = text("hash")
    val lastLogin = long("lastLogin")
}