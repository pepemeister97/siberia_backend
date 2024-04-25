package siberia.modules.product.data.dto.groups

import kotlinx.serialization.Serializable

@Serializable
data class ProductGroupCreateDto (
    val name: String,
    var products: List<Int> = listOf()
)