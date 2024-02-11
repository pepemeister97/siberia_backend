package siberia.modules.brand.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BrandUpdateDto (
    var name: String? = null
)