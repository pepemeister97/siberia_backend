package siberia.conf

data class RequestStatusConf (
    val open: Int,
    val created: Int,
    val creationCancelled: Int,
    val inProgress: Int,
    val processingCancelled: Int,
    val delivered: Int,
    val notDelivered: Int,
    val failed: Int,
    val processed: Int,
)