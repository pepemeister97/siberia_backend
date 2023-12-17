package siberia.modules.transaction.data.models

import siberia.utils.database.BaseIntIdTable

object TransactionStatusModel : BaseIntIdTable() {
    val name = text("name")
}