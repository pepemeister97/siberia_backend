package siberia.modules.category.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryRemoveResultDto (
    val success: Boolean,
    val message: String
)