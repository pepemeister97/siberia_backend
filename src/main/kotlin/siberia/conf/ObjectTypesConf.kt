package siberia.conf

data class ObjectTypesConf (
    val userEvent: Int,
    val stockEvent: Int,
    val roleEvent: Int,
    val productEvent: Int,
    val massiveProductUpdateEvent: Int,
    val brandEvent: Int,
    val collectionEvent: Int,
    val categoryEvent: Int,
    val transactionEvent: Int, //Not implemented
    val productGroupEvent: Int,
    val productBulkCreate: Int,
    val userRightsEvent: Int,
)