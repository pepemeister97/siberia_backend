package siberia.modules.product.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.utils.database.BaseIntIdTable

object ProductCategoryToProductCategoryModel : BaseIntIdTable() {
    val parent = reference("parent", ProductCategoryModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val child = reference("child", ProductCategoryModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
}