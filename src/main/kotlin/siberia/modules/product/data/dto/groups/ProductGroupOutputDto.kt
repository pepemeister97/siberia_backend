package siberia.modules.product.data.dto.groups

import kotlinx.serialization.Serializable

@Serializable
data class ProductGroupOutputDto (
    val id: Int,
    val name: String
)