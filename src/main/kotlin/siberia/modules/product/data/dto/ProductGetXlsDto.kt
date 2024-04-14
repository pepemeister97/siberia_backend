package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductGetXlsDto(
    val searchFilters: ProductSearchFilterDto,
    val fieldsDemand: ProductFieldsDemandDto
)