package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductCreateDto (
    var photoName: String?,
    var photoBase64: String?,
    var vendorCode: String?,
    var eanCode: String?,
    var barcode: String?,
    var brand: Int? = null,
    var name: String?,
    var description: String?,
    var distributorPrice: Double?,
    var professionalPrice: Double?,
    var commonPrice: Double?,
    var category: Int? = null,
    var collection: Int? = null,
    var color: String?,
    var amountInBox: Int?,
    var expirationDate: Long?,
    var link: String?,
    //We need it to easily rollback remove events without re-upload files
    var fileAlreadyUploaded: Boolean? = false,
//    Future iterations
//    val size: Double,
//    val volume: Double,
)