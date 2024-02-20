package siberia.modules.product.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.update
import siberia.modules.brand.data.models.BrandModel
import siberia.modules.category.data.models.CategoryModel
import siberia.modules.collection.data.models.CollectionModel
import siberia.modules.product.data.dto.ProductMassiveUpdateDto
import siberia.utils.database.BaseIntIdTable

object ProductModel: BaseIntIdTable() {
    val photo = text("photo")
    val vendorCode = text("vendor_code")
    val barcode = text("barcode").nullable().default(null)
    val brand = reference("brand", BrandModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable().default(null)
    val name = text("name")
    val description = text("description")
    val lastPurchasePrice = double("last_purchase_price").nullable().default(null)
    val cost = double("cost").nullable().default(null)
    val lastPurchaseDate = long("last_purchase_date").nullable().default(null)
    val distributorPrice = double("distributor_price")
    val professionalPrice = double("professional_price")
    val commonPrice = double("common_price")
    val category = reference("category", CategoryModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable()
    val collection = reference("collection", CollectionModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable().default(null)
    val color = text("color")
    val amountInBox = integer("amount_in_box")
    val expirationDate = long("expiration_date")
    val link = text("link")

    val distributorPercent = double("distributor_percent").default(1.0)
    val professionalPercent = double("professional_percent").default(1.0)
    val eanCode = text("ean_code")
    //Future iterations
//    val size = double("size").default(1.0)
//    val volume = double("volume").default(1.0)

    fun updateBatch(products: List<Int>, productMassiveUpdateDto: ProductMassiveUpdateDto) {
        ProductModel.update({ ProductModel.id inList products }) {
            if (productMassiveUpdateDto.brand != 0 && productMassiveUpdateDto.brand != null)
                it[brand] = productMassiveUpdateDto.brand
            else if (productMassiveUpdateDto.brand == 0)
                it[brand] = null

            if (productMassiveUpdateDto.collection != 0 && productMassiveUpdateDto.collection != null)
                it[collection] = productMassiveUpdateDto.collection
            else if (productMassiveUpdateDto.collection == 0)
                it[collection] = null

            if (productMassiveUpdateDto.category != 0 && productMassiveUpdateDto.category != null)
                it[category] = productMassiveUpdateDto.category
            else if (productMassiveUpdateDto.category == 0)
                it[category] = null

            if (productMassiveUpdateDto.name != null)
                it[name] = productMassiveUpdateDto.name!!

            if (productMassiveUpdateDto.description != null)
                it[description] = productMassiveUpdateDto.description!!

            if (productMassiveUpdateDto.commonPrice != null)
                it[commonPrice] = productMassiveUpdateDto.commonPrice!!

            if (productMassiveUpdateDto.color != null)
                it[color] = productMassiveUpdateDto.color!!

            if (productMassiveUpdateDto.amountInBox != null)
                it[amountInBox] = productMassiveUpdateDto.amountInBox!!

            if (productMassiveUpdateDto.expirationDate != null)
                it[expirationDate] = productMassiveUpdateDto.expirationDate!!

            if (productMassiveUpdateDto.link != null)
                it[link] = productMassiveUpdateDto.link!!

            if (productMassiveUpdateDto.distributorPercent != null)
                it[distributorPercent] = productMassiveUpdateDto.distributorPercent

            if (productMassiveUpdateDto.professionalPercent != null)
                it[professionalPercent] = productMassiveUpdateDto.professionalPercent
        }
    }
}