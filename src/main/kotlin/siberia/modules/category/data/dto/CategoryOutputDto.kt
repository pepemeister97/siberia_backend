package siberia.modules.category.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryOutputDto (
    val id: Int,
    val name: String,
    var children: List<CategoryOutputDto> = listOf()
)