package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.stock.data.models.StockProductsListMapped

@Serializable
data class  ProductRollbackDto (
    val id: Int,
    val photo: List<Int>,
    val vendorCode: String,
    val eanCode: String,
    val barcode: String?,
    val brand: Int?,
    val name: String,
    val description: String,
    val lastPurchasePrice: Double?,
    val cost: Double?,
    val lastPurchaseDate: Long?,
    val distributorPercent: Double,
    val professionalPercent: Double,
    val commonPrice: Double,
    val category: Int?,
    val collection: Int?,
    val color: String,
    val amountInBox: Int,
    val expirationDate: Long,
    val link: String,
    val quantity: Double,
    val stocksRelations: MutableMap<Int, StockProductsListMapped> = mutableMapOf(),
    val offertaPrice: Double?,
    val fileAlreadyUploaded: Boolean = true,
) {
    val createDto: ProductCreateDto get() = ProductCreateDto(
        photo, vendorCode, eanCode, barcode,
        brand, name, description, commonPrice, category,
        collection, color, amountInBox,
        expirationDate, link, offertaPrice, distributorPercent, professionalPercent
    )
}