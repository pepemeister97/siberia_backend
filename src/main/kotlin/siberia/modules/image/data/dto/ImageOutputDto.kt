package siberia.modules.image.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageOutputDto(
    val id : Int,
    val name : String,
    val url : String,
    val author : String?,
    val description : String?,
)