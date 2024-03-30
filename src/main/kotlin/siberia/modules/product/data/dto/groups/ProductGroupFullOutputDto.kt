package siberia.modules.product.data.dto.groups

import kotlinx.serialization.Serializable
import siberia.modules.product.data.dto.ProductListItemOutputDto

@Serializable
data class ProductGroupFullOutputDto (
    val id: Int,
    val name: String,
    val products: List<ProductListItemOutputDto>
)