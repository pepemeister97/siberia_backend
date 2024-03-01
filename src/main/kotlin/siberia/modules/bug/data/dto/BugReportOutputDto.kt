package siberia.modules.bug.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BugReportOutputDto(
    val id: Int,
    val user: String,
    val description: String
)
