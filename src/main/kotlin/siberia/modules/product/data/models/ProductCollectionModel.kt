package siberia.modules.product.data.models

import siberia.utils.database.BaseIntIdTable

object ProductCollectionModel: BaseIntIdTable() {
    val name = text("name")
}