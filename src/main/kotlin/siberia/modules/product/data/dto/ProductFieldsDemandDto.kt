package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.brand.data.dto.BrandOutputDto
import siberia.modules.category.data.dto.CategoryOutputDto
import siberia.modules.collection.data.dto.CollectionOutputDto
import siberia.utils.database.FieldFilterWrapper

@Serializable
data class ProductFieldsDemandDto (
    val id: Boolean?,
    val photo: Boolean?,
    val vendorCode: Boolean?,
    val barcode: Boolean?,
    val brand: Boolean?,
    val name: Boolean?,
    val description: Boolean?,
    val lastPurchasePrice: Boolean?,
    val cost: Boolean?,
    val lastPurchaseDate: Boolean?,
    val distributorPrice: Boolean?,
    val professionalPrice: Boolean?,
    val distributorPercent: Boolean?,
    val professionalPercent: Boolean?,
    val commonPrice: Boolean?,
    val category: Boolean?,
    val collection: Boolean?,
    val color: Boolean?,
    val amountInBox: Boolean?,
    val expirationDate: Boolean?,
    val link: Boolean?,
    val quantity: Boolean?,
    val offerPrice: Boolean?
)