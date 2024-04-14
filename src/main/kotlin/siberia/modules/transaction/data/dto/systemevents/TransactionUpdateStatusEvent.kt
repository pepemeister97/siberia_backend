package siberia.modules.transaction.data.dto.systemevents

import siberia.conf.AppConf

data class TransactionUpdateStatusEvent(
    override val author: String,
    val updatedTransactionStockName: String,
    override val eventObjectId: Int,
    val updateToStatus: String
) : TransactionEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Status of transaction (id = $eventObjectId) for $updatedTransactionStockName stock was updated to $updateToStatus"
    override val eventObjectName: String
        get() = eventObjectId.toString()
}