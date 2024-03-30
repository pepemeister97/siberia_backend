package siberia.modules.product.data.dto.groups

import kotlinx.serialization.Serializable

@Serializable
data class ProductGroupUpdateDto (
    val name: String? = null,
    val products: List<Int>? = null
)