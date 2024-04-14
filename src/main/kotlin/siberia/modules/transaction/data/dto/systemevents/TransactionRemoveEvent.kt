package siberia.modules.transaction.data.dto.systemevents

import siberia.conf.AppConf

data class TransactionRemoveEvent(
    override val author: String,
    val removedTransactionStockName: String,
    override val eventObjectId: Int
) : TransactionEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "Transaction (id = $eventObjectId) for $removedTransactionStockName stock was removed."
    override val eventObjectName: String
        get() = eventObjectId.toString()
}