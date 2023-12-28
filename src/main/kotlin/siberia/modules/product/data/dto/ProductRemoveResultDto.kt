package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductRemoveResultDto (
    val success: Boolean,
    val message: String
)