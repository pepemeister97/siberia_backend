package siberia.modules.product.data.dto.groups

data class ProductGroupUpdateDto (
    val name: String? = null,
    val products: List<Int>? = null
)