package siberia.modules.collection.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionOutputDto (
    val id: Int,
    val name: String
)