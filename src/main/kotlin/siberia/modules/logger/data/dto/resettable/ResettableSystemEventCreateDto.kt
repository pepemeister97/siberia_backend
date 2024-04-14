package siberia.modules.logger.data.dto.resettable

import siberia.modules.logger.data.dto.SystemEventCreateDto

abstract class ResettableSystemEventCreateDto : SystemEventCreateDto() {
    abstract val rollbackInstance: String
    abstract val rollbackRoute: String
}