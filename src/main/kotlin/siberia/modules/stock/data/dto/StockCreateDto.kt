package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StockCreateDto (
    val name: String,
    val address: String,
)