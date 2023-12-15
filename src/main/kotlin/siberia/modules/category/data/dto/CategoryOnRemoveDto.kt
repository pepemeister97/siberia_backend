package siberia.modules.category.data.dto

data class CategoryOnRemoveDto (
    val removeChildren: Boolean,
    val transferChildrenTo: Int? = null
)