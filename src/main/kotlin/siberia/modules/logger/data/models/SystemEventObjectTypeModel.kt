package siberia.modules.logger.data.models

import siberia.utils.database.BaseIntIdTable

object SystemEventObjectTypeModel: BaseIntIdTable() {
    val name = text("name")
}