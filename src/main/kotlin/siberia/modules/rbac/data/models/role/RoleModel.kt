package siberia.modules.rbac.data.models.role

import siberia.utils.database.BaseIntIdTable

object RoleModel: BaseIntIdTable() {
    val name = text("name")
    val description = text("description").nullable().default(null)
}