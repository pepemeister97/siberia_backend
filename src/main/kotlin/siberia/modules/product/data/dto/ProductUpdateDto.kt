package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductUpdateDto (
    var photoName: String? = null,
    var photoBase64: String? = null,
    var vendorCode: String? = null,
    var barcode: String? = null,
    //If null -> dont update
    //If 0 -> set null
    //if not zero and not null -> update to what set
    var brand: Int? = null,
    var name: String? = null,
    var description: String? = null,
//    var distributorPrice: Double? = null,
//    var professionalPrice: Double? = null,
    var commonPrice: Double? = null,
    var category: Int? = null,
    //If null -> dont update
    //If 0 -> set null
    //if not zero and not null -> update to what set
    var collection: Int? = null,
    var color: String? = null,
    var amountInBox: Int? = null,
    var expirationDate: Long? = null,
    var link: String? = null,
    var fileAlreadyUploaded: Boolean = false,
    val distributorPercent: Double? = null,
    val professionalPercent: Double? = null,
    val eanCode: String? = null

//    Future iterations
//    val size: Double? = null,
//    val volume: Double? = null,
)
