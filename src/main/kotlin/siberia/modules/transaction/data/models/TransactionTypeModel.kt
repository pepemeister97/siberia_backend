package siberia.modules.transaction.data.models

import siberia.utils.database.BaseIntIdTable

object TransactionTypeModel : BaseIntIdTable() {
    val name = text("name")
}