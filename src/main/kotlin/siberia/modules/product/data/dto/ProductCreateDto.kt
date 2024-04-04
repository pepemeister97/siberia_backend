package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductCreateDto (
    var photoList: List<Int>? = listOf(),
    var vendorCode: String?,
    var eanCode: String?,
    var barcode: String?,
    var brand: Int? = null,
    var name: String?,
    var description: String?,
    var commonPrice: Double?,
    var category: Int?,
    var collection: Int? = null,
    var color: String?,
    var amountInBox: Int?,
    var expirationDate: Long?,
    var link: String?,
    var distributorPercent: Double?,
    var professionalPercent: Double?
//    Future iterations
//    val size: Double,
//    val volume: Double,
)