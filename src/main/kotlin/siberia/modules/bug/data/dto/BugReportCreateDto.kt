package siberia.modules.bug.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BugReportCreateDto(
    val description : String
)
