package siberia.modules.notifications.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntIdTable

object NotificationModel : BaseIntIdTable() {
    val target = reference("target_user", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val watched = bool("watched").default(false)
    val description = text("description")
    val type = reference("type", NotificationTypeModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val domain = reference("domain", NotificationDomainModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
}