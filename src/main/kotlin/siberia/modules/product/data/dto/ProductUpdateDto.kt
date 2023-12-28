package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductUpdateDto (
    val photo: String? = null,
    val vendorCode: String? = null,
    val barcode: String? = null,
    //If null -> dont update
    //If 0 -> set null
    //if not zero and not null -> update to what set
    val brand: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val purchasePrice: Double? = null,
    val distributorPrice: Double? = null,
    val professionalPrice: Double? = null,
    val commonPrice: Double? = null,
    val category: Int? = null,
    //If null -> dont update
    //If 0 -> set null
    //if not zero and not null -> update to what set
    val collection: Int? = null,
    val color: String? = null,
    val amountInBox: Int? = null,
    val expirationDate: Long? = null,
    val link: String? = null

//    Future iterations
//    val size: Double? = null,
//    val volume: Double? = null,
)