package siberia.modules.logger.data.models

import siberia.utils.database.BaseIntIdTable

object SystemEventTypeModel: BaseIntIdTable() {
    val name = text("name")
}