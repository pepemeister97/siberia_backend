package siberia.modules.auth.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.stock.data.dto.StockOutputDto
import siberia.modules.transaction.data.dto.TransactionFullOutputDto

@Serializable
data class AuthenticatedStockOutputDto (
    val stockData: StockOutputDto,
    val operationsAccess: MobileOperationAccessDto,
    val type: String,
    val transactionData: TransactionFullOutputDto? = null
)