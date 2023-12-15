package siberia.modules.collection.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionRemoveResultDto (
    val success: Boolean,
    val message: String
)