package siberia.modules.product.data.dto.groups

import kotlinx.serialization.Serializable

@Serializable
data class ProductGroupUpdateDto (
    var name: String? = null,
    var products: List<Int>? = null
)