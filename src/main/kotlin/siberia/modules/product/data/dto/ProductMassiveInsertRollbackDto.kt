package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductMassiveInsertRollbackDto (
    var productsList: List<ProductListItemOutputDto> = listOf()
)