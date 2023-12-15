package siberia.modules.product.data.dto

import siberia.utils.database.PaginationOutputDto

data class ProductSearchDto (
    val pagination: PaginationOutputDto,
    val filters: ProductSearchFilterDto
)