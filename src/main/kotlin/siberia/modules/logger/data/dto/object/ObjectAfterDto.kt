package siberia.modules.logger.data.dto.`object`

import kotlinx.serialization.Serializable

@Serializable
abstract class ObjectAfterDto <T>(val data: T, val editedFields: List<String>)