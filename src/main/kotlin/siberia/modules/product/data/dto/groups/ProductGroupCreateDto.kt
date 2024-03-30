package siberia.modules.product.data.dto.groups

import kotlinx.serialization.Serializable

@Serializable
data class ProductGroupCreateDto (
    val name: String,
    val products: List<Int> = listOf()
)