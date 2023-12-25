package siberia.modules.notifications.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationsFilterDto (
    val new: Boolean? = null,
    val type: List<Int>? = null,
    val domain: List<Int>? = null,
    val description: String? = null
)