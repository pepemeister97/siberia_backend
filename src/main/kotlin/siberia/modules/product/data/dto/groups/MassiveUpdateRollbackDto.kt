package siberia.modules.product.data.dto.groups

import kotlinx.serialization.Serializable
import siberia.modules.product.data.dto.ProductOutputDto
import siberia.modules.product.data.dto.ProductUpdateDto

@Serializable
data class MassiveUpdateRollbackDto (
    val massiveUpdateDto: MassiveUpdateDto,
    val productsData: List<Pair<ProductUpdateDto, ProductOutputDto>>
)