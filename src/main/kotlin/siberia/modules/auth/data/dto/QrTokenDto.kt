package siberia.modules.auth.data.dto

data class QrTokenDto (
    val userId: Int,
    val stockId: Int? = null,
    val transactionId: Int? = null,
)