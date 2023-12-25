package siberia.modules.notifications.data.models

import siberia.utils.database.BaseIntIdTable

object NotificationTypeModel : BaseIntIdTable() {
    val name = text("name")
}