package siberia.modules.category.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryOutputDto (
    val id: Int,
    val name: String,
    var children: List<CategoryOutputDto> = listOf(),
    var childrenRemoved: Boolean = false,
    var parent: Int = 0,
) {
    val createDto: CategoryInputDto get() = CategoryInputDto(parent, name)
}