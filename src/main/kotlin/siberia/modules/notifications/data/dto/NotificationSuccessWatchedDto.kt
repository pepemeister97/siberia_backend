package siberia.modules.notifications.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationSuccessWatchedDto (
    val success: Boolean
)