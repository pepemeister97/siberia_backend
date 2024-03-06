package siberia.modules.gallery.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageSearchFilterDto(
    val name : String? = null,
    val rangeStart: Long? = null,
    val rangeEnd: Long? = null
)