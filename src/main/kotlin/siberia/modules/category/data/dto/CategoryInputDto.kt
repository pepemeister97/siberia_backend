package siberia.modules.category.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryInputDto (
    val parent: Int? = null,
    val name: String,
)