package siberia.modules.gallery.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageUpdateDto (
    val name: String? = null,
    val description: String? = null
)