package siberia.modules.category.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.utils.database.BaseIntIdTable

object CategoryToCategoryModel : BaseIntIdTable() {
    val parent = reference("parent", CategoryModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val child = reference("child", CategoryModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
}