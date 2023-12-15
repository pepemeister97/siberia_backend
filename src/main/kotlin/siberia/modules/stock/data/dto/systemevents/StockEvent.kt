package siberia.modules.stock.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.SystemEventCreateDto

abstract class StockEvent: SystemEventCreateDto() {
    override val eventObjectType: Int
        get() = AppConf.objectTypes.stockEvent
}