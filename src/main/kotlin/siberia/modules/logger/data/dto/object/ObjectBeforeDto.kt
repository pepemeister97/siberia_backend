package siberia.modules.logger.data.dto.`object`

import kotlinx.serialization.Serializable

@Serializable
abstract class ObjectBeforeDto <T> (val data: T)