package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductParseResultDto (
    val brandMap: Map<Int, String>,
    val collectionMap: Map<Int, String>,
    val categoryMap: Map<Int, String>,
    val createList: List<ProductCreateDto>
)