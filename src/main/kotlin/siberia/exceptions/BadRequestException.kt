package siberia.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class BadRequestException(
    override val message: String
) : BaseException(404, "Bad request", message)