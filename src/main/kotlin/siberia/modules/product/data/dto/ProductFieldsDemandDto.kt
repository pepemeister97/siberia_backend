package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductFieldsDemandDto (
    val id: Boolean? = null,
    val photo: Boolean? = null,
    val vendorCode: Boolean? = null,
    val eanCode: Boolean? = null,
    val barcode: Boolean? = null,
    val brand: Boolean? = null,
    val name: Boolean? = null,
    val description: Boolean? = null,
    val lastPurchasePrice: Boolean? = null,
    val cost: Boolean? = null,
    val lastPurchaseDate: Boolean? = null,
    val distributorPrice: Boolean? = null,
    val professionalPrice: Boolean? = null,
    val distributorPercent: Boolean? = null,
    val professionalPercent: Boolean? = null,
    val commonPrice: Boolean? = null,
    val category: Boolean? = null,
    val collection: Boolean? = null,
    val color: Boolean? = null,
    val amountInBox: Boolean? = null,
    val expirationDate: Boolean? = null,
    val link: Boolean? = null,
    val quantity: Boolean? = null,
    val offertaPrice: Boolean? = null
)