package siberia.modules.product.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.utils.database.BaseIntIdTable

object ProductModel: BaseIntIdTable() {
    val name = text("name")
    val price = double("price")
    val color = text("color")
    val category = reference("category", ProductCategoryModel, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
    val collection = reference("collection", ProductCollectionModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable().default(null)
}