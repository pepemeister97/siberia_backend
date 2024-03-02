package siberia.modules.transaction.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionRemoveResultDto (
    val success: Boolean,
    val message: String
)