package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StockOutputDto (
    val id: Int,
    val name: String,
    val address: String,
)