package siberia.modules.notifications.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationCreateDto (
    val targetId: Int,
    val typeId: Int,
    val domainId: Int,
    val description: String
)