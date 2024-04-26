package siberia.modules.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MobileOperationAccessDto (
    val arrivalsManaging: Boolean,
    val salesManaging: Boolean,
    val transfersManaging: Boolean,
    val transfersProcessing: Boolean
)