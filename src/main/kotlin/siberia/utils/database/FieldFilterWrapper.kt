package siberia.utils.database

import kotlinx.serialization.Serializable

@Serializable
data class FieldFilterWrapper <T> (
    val specificValue: T?,
    val topBound: T?,
    val bottomBound: T?
)