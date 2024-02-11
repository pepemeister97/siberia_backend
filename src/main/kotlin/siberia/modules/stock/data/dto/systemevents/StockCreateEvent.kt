package siberia.modules.stock.data.dto.systemevents

import siberia.conf.AppConf

data class StockCreateEvent(
    override val author: String, val createdStockName: String
) : StockEvent() {
    override val eventObjectId: Int
        get() = 0
    override val rollbackInstance: String
        get() = ""
    override val eventType: Int
        get() = AppConf.eventTypes.createEvent
    override val eventDescription: String
        get() = "Stock $createdStockName was created."
    override val eventObjectName: String
        get() = createdStockName
}