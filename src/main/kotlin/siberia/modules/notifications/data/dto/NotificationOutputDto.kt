package siberia.modules.notifications.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationOutputDto (
    val id: Int,
    val watched: Boolean,
    val type: NotificationTypeOutputDto,
    val domain: NotificationDomainOutputDto,
    val description: String
) {
    @Serializable
    data class NotificationList(
        val notifications: List<NotificationOutputDto>
    )
}