package siberia.modules.stock.data

import siberia.modules.product.data.dto.ProductOutputDto

data class StockFullOutputDto (
    val id: Int,
    val name: String,
    val address: String,
    val products: List<ProductOutputDto>
)