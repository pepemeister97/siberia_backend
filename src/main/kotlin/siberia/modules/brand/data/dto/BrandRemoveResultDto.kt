package siberia.modules.brand.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BrandRemoveResultDto (
    val success: Boolean,
    val message: String
)