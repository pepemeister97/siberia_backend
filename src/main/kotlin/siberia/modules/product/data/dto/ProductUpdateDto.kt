package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductUpdateDto (
    val photo: String? = null,
    val vendorCode: String? = null,
    val barcode: String? = null,
    val brand: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val purchasePrice: Double? = null,
    val distributorPrice: Double? = null,
    val professionalPrice: Double? = null,
    val commonPrice: Double? = null,
    val category: Int? = null,
    val collection: Int? = null,
    val color: String? = null,
    val amountInBox: Int? = null,
    val expirationDate: Int? = null,
    val link: String? = null

//    Future iterations
//    val size: Double? = null,
//    val volume: Double? = null,
)