package siberia.modules.notifications.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDomainOutputDto (
    val id: Int,
    val name: String
)