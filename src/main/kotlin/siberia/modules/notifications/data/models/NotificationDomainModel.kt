package siberia.modules.notifications.data.models

import siberia.utils.database.BaseIntIdTable

object NotificationDomainModel : BaseIntIdTable() {
    val name = text("name")
}