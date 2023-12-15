package siberia.modules.stock.data.dto.systemevents

import siberia.conf.AppConf

data class StockUpdateEvent(
    override val author: String, val updatedStockName: String
) : StockEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Stock $updatedStockName was updated."
    override val eventObjectName: String
        get() = updatedStockName
}