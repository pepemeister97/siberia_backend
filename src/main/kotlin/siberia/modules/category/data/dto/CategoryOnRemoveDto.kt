package siberia.modules.category.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryOnRemoveDto (
    val removeChildren: Boolean,
    val transferChildrenTo: Int? = null
)