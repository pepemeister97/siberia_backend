package siberia.modules.user.data.models

import siberia.utils.database.BaseIntIdTable

object UserModel: BaseIntIdTable() {
    val name = text("name")
    val login = text("login")
    val hash = text("hash")
}