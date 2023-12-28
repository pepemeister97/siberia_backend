package siberia.utils.database

import kotlinx.serialization.Serializable

@Serializable
data class FieldFilterWrapper <T> (
    val specificValue: T? = null,
    val topBound: T? = null,
    val bottomBound: T? = null
)