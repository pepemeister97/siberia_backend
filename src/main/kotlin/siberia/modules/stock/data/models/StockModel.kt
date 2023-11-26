package siberia.modules.stock.data.models

import siberia.utils.database.BaseIntIdTable

object StockModel: BaseIntIdTable() {
    val name = text("name")
    val address = text("address")
}