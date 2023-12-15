package siberia.modules.product.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.brand.data.models.BrandModel
import siberia.modules.collection.data.models.CollectionModel
import siberia.utils.database.BaseIntIdTable

object ProductModel: BaseIntIdTable() {
    val name = text("name")
    val price = double("price")
    val color = text("color")
    val category = reference("category", ProductCategoryModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable()
    val collection = reference("collection", CollectionModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable().default(null)
    val brand = reference("collection", BrandModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable().default(null)
}