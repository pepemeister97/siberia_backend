package siberia.modules.bug.data.dto

data class BugReportSearchFilterDto(
    val author: String? = null,
    val rangeStart: Long? = null,
    val rangeEnd: Long? = null
)