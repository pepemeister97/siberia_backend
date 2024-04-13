package siberia.modules.product.data.dto.groups

data class ProductGroupUpdateRollbackDto (
    val name: String?,
    val products: List<Int> = listOf()
)