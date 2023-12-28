package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable
import siberia.utils.database.PaginationDto

@Serializable
data class ProductSearchDto (
    val pagination: PaginationDto? = null,
    val filters: ProductSearchFilterDto? = null
)