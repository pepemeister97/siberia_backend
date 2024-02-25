package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductCreateDto (
    var photoName: String,
    var photoBase64: String,
    val vendorCode: String,
    val eanCode: String,
    val barcode: String?,
    val brand: Int? = null,
    val name: String,
    val description: String,
    val distributorPrice: Double,
    val professionalPrice: Double,
    val commonPrice: Double,
    val category: Int? = null,
    val collection: Int? = null,
    val color: String,
    val amountInBox: Int,
    val expirationDate: Long,
    val link: String,
    //We need it to easily rollback remove events without re-upload files
    val fileAlreadyUploaded: Boolean = false,
//    Future iterations
//    val size: Double,
//    val volume: Double,
)