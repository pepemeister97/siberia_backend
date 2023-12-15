package siberia.modules.logger.data.dto

import java.time.LocalDateTime

data class SystemEventSearchFilter (
    val userName: String? = null,
    val range: Pair<LocalDateTime?, LocalDateTime?>? = null,
    val type: Int? = null,
    val objectType: Int? = null
)