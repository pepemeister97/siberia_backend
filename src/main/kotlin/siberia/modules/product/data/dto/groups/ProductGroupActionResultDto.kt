package siberia.modules.product.data.dto.groups

import kotlinx.serialization.Serializable

@Serializable
data class ProductGroupActionResultDto (
    val success: Boolean,
    val message: String
)