package siberia.modules.collection.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionUpdateDto (
    var name: String? = null
)