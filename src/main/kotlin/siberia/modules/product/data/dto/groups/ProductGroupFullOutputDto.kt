package siberia.modules.product.data.dto.groups

import siberia.modules.product.data.dto.ProductListItemOutputDto

data class ProductGroupFullOutputDto (
    val id: Int,
    val name: String,
    val products: List<ProductListItemOutputDto>
)