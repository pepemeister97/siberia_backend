package siberia.modules.product.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.utils.database.BaseIntIdTable

object ProductToImageModel: BaseIntIdTable() {
    val photo = integer("photo")
    val product = reference("product", ProductModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
}