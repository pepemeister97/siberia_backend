package siberia.modules.category.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryUpdateDto (
    var name: String? = null,
    var parent: Int? = null
)