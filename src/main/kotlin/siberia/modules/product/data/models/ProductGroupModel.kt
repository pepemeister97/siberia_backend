package siberia.modules.product.data.models

import siberia.utils.database.BaseIntIdTable

object ProductGroupModel : BaseIntIdTable() {
    val name = text("name")

}