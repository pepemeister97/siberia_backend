package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.product.data.dto.ProductListItemOutputDto

@Serializable
data class StockFullOutputDto (
    val id: Int,
    val name: String,
    val address: String,
    val products: List<ProductListItemOutputDto>
) {
    fun toRollbackDto(relatedUsers: Map<Int, List<Int>>, relatedRoles: Map<Int, List<Int>>): StockRollbackRemoveDto {
        return StockRollbackRemoveDto(
            id, name, address, products, relatedUsers, relatedRoles
        )
    }
}