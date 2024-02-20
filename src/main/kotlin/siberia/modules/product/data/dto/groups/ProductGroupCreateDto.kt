package siberia.modules.product.data.dto.groups

data class ProductGroupCreateDto (
    val name: String,
    val products: List<Int> = listOf()
)