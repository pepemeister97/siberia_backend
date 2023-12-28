package siberia.modules.product.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.brand.data.models.BrandModel
import siberia.modules.category.data.models.CategoryModel
import siberia.modules.collection.data.models.CollectionModel
import siberia.utils.database.BaseIntIdTable

object ProductModel: BaseIntIdTable() {
    val photo = text("photo")
    val vendorCode = text("vendor_code")
    val barcode = text("barcode").nullable().default(null)
    val brand = reference("brand", BrandModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable().default(null)
    val name = text("name")
    val description = text("description")
    val purchasePrice = double("purchase_price")
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

    //Future iterations
//    val size = double("size").default(1.0)
//    val volume = double("volume").default(1.0)
}