package siberia.modules.auth.data.models.role

import siberia.utils.database.BaseIntIdTable

object RoleModel: BaseIntIdTable() {
    val name = text("name")
}