package siberia.modules.stock.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto

abstract class StockEvent: ResettableSystemEventCreateDto() {
    override val rollbackRoute: String
        get() = "stock"
    override val eventObjectType: Int
        get() = AppConf.objectTypes.stockEvent
}