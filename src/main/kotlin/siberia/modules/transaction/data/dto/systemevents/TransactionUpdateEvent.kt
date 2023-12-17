package siberia.modules.transaction.data.dto.systemevents

import siberia.conf.AppConf
import siberia.modules.stock.data.dto.systemevents.StockEvent

data class TransactionUpdateEvent(
    override val author: String, val updatedTransactionStockName: String, val updatedTransactionId: Int
) : StockEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Transaction (id = $updatedTransactionId) for $updatedTransactionStockName stock was updated."
    override val eventObjectName: String
        get() = updatedTransactionId.toString()
}