package siberia.modules.gallery.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageCreateDto(
    val photoName : String,
    val name: String,
    val imageBase64 : String,
    val description: String
)
