package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StockUpdateDto (
    var name: String? = null,
    var address: String? = null
)