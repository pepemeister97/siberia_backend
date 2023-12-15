package siberia.modules.collection.data.models

import siberia.utils.database.BaseIntIdTable

object CollectionModel : BaseIntIdTable() {
    val name = text("name")
}