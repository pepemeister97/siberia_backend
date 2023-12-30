package siberia.modules.stock.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.product.data.models.ProductModel
import siberia.utils.database.BaseIntIdTable

object StockToProductModel: BaseIntIdTable() {
    val product = reference("product", ProductModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val stock = reference("stock", StockModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val amount = double("amount")
}