package siberia.modules.brand.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BrandOutputDto (
    val id: Int,
    val name: String
)