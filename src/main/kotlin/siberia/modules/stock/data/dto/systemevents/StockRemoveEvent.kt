package siberia.modules.stock.data.dto.systemevents

import siberia.conf.AppConf

data class StockRemoveEvent(
    override val author: String, val removedStockName: String
) : StockEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "Stock $removedStockName was removed."
    override val eventObjectName: String
        get() = removedStockName
}