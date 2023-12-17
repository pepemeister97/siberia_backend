package siberia.modules.transaction.data.dto.status

import kotlinx.serialization.Serializable

@Serializable
data class TransactionStatusOutputDto (
    val id: Int,
    val name: String,
)