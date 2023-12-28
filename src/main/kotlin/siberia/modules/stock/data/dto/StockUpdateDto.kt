package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StockUpdateDto (
    val name: String? = null,
    val address: String? = null
)