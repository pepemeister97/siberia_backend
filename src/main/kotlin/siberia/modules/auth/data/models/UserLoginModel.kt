package siberia.modules.auth.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntIdTable

object UserLoginModel: BaseIntIdTable() {
    val userId = reference("user_id", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val lastLogin = long("last_login")
}