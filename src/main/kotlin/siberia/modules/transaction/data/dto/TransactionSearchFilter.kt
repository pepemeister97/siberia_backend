package siberia.modules.transaction.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionSearchFilter (
    val status: List<Int>? = null,
    val type: List<Int>? = null,
    val to: List<Int>? = null,
    val from: List<Int>? = null,
    val showClosed: Boolean = false,
)
