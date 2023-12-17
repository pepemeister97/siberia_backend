package siberia.conf

data class RequestStatusConf (
    val created: Int,
    val open: Int,
    val creationCancelled: Int,
    val processed: Int,
    val inProgress: Int,
    val processingCancelled: Int,
    val delivered: Int,
    val notDelivered: Int,
    val deliveryCancelled: Int,
    val failed: Int,
)