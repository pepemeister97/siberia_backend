package siberia.modules.transaction.data.dto.systemevents

import siberia.conf.AppConf

data class TransactionCreateEvent(
    override val author: String,
    val createdTransactionStockName: String,
    override val eventObjectId: Int
) : TransactionEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.createEvent
    override val eventDescription: String
        get() = "Transaction (id = $eventObjectId) for $createdTransactionStockName stock was created."
    override val eventObjectName: String
        get() = eventObjectId.toString()
}