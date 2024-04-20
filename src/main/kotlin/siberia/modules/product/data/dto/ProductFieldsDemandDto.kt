package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductFieldsDemandDto (
    val id: String? = null,
    val photo: String? = null,
    val vendorCode: String? = null,
    val eanCode: String? = null,
    val barcode: String? = null,
    val brand: String? = null,
    val name: String? = null,
    val description: String? = null,
    val lastPurchasePrice: String? = null,
    val cost: String? = null,
    val lastPurchaseDate: String? = null,
    val distributorPrice: String? = null,
    val professionalPrice: String? = null,
    val distributorPercent: String? = null,
    val professionalPercent: String? = null,
    val commonPrice: String? = null,
    val category: String? = null,
    val collection: String? = null,
    val color: String? = null,
    val amountInBox: String? = null,
    val expirationDate: String? = null,
    val link: String? = null,
    val quantity: String? = null,
    val offertaPrice: String? = null
)