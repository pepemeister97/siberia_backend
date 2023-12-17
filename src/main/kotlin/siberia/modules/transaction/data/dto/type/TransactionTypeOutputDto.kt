package siberia.modules.transaction.data.dto.type

import kotlinx.serialization.Serializable

@Serializable
data class TransactionTypeOutputDto (
    val id: Int,
    val name: String,
)