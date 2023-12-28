package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable
import siberia.utils.database.PaginationDto

@Serializable
data class StockSearchDto (
    val filters: StockSearchFilterDto? = null,
    val pagination: PaginationDto? = null,
)