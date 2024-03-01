package siberia.modules.bug.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BugReportSearchFilterDto(
    val author: String? = null,
    val rangeStart: Long? = null,
    val rangeEnd: Long? = null
)