package siberia.modules.product.data.dto

import siberia.utils.database.PaginationDto

data class ProductSearchDto (
    val pagination: PaginationDto,
    val filters: ProductSearchFilterDto
)