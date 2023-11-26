package siberia.modules.product.data.dto

data class ProductCategoryOutputDto (
    val id: Int,
    val name: String,
    val parent: ProductCategoryOutputDto? = null,
    val children: List<ProductCategoryOutputDto> = listOf()
)