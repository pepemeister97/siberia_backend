package siberia.modules.product.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssortmentExportTemplateDto(
    val brandSheetTitle: String,
    val collectionSheetTitle: String,
    val categorySheetTitle: String,
    val productSheetTitle: String
)
