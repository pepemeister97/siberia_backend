package siberia.modules.gallery.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetPathsInputDto (
    val ids: List<Int>
)