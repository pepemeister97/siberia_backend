package siberia.modules.transaction.data.dto.systemevents

import siberia.conf.AppConf

data class TransactionRemoveEvent(
    override val author: String, val removedTransactionStockName: String, val removedTransactionId: Int
) : TransactionEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "Transaction (id = $removedTransactionId) for $removedTransactionStockName stock was removed."
    override val eventObjectName: String
        get() = removedTransactionId.toString()
}