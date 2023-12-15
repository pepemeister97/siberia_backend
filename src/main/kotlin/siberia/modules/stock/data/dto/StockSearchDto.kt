package siberia.modules.stock.data.dto

import siberia.utils.database.PaginationDto

data class StockSearchDto (
    val filters: StockSearchFilterDto,
    val pagination: PaginationDto,
)