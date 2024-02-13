package siberia.modules.transaction.data.dto.systemevents

import siberia.conf.AppConf

data class TransactionUpdateStatusEvent(
    override val author: String, val updatedTransactionStockName: String, val updatedTransactionId: Int, val updateToStatus: String
) : TransactionEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Status of transaction (id = $updatedTransactionId) for $updatedTransactionStockName stock was updated to $updateToStatus"
    override val eventObjectName: String
        get() = updatedTransactionId.toString()
}