package siberia.modules.bug.data.models

import siberia.utils.database.BaseIntIdTable

object BugReportModel : BaseIntIdTable() {
    val user = text("user")
    val description = text("description")
}