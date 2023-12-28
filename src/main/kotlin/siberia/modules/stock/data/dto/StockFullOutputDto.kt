package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.product.data.dto.ProductOutputDto

@Serializable
data class StockFullOutputDto (
    val id: Int,
    val name: String,
    val address: String,
    val products: List<ProductOutputDto>
)