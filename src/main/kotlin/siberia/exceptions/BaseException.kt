package siberia.exceptions

import kotlinx.serialization.Serializable

@Serializable
abstract class BaseException (
    val httpStatusCode: Int,
    val httpStatusText: String,
    var data: String? = null
): Exception(data)
