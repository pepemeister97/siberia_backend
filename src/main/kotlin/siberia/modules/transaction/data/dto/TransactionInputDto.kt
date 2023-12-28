package siberia.modules.transaction.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionInputDto (
    val from: Int?,
    val to: Int?,
    val type: Int,
    val products: List<TransactionFullOutputDto.TransactionProductDto>
)