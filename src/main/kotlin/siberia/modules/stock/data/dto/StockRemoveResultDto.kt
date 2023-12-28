package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StockRemoveResultDto (
    val success: Boolean,
    val message: String
)