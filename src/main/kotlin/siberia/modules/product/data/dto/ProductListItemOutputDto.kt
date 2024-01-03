package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductListItemOutputDto (
    val id: Int,
    val name: String,
    val vendorCode: String,
    val quantity: Int = 0,
    val price: Double
)