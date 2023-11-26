package siberia.modules.product.data.models

import siberia.utils.database.BaseIntIdTable

object ProductCategoryModel: BaseIntIdTable() {
    val name = text("name")
}