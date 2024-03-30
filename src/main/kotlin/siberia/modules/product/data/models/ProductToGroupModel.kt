package siberia.modules.product.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.exceptions.BadRequestException
import siberia.modules.product.data.dto.ProductListItemOutputDto
import siberia.utils.database.BaseIntIdTable

object ProductToGroupModel : BaseIntIdTable() {
    val product = reference("product", ProductModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val group = reference("group", ProductGroupModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)


    fun getProducts(groupId: Int): List<ProductListItemOutputDto> = transaction {
        leftJoin(ProductModel)
        .slice(ProductModel.id, ProductModel.name, ProductModel.vendorCode)
        .select { group eq groupId }
        .map {
            ProductListItemOutputDto(
                id = it[ProductModel.id].value,
                name = it[ProductModel.name],
                vendorCode = it[ProductModel.vendorCode],
                price = 0.0
            )
        }
    }

    fun setProducts(groupId: Int, products: List<Int>) = transaction {
        if (ProductModel.select {
            ProductModel.id inList products
        }.count() != products.size.toLong())
            throw BadRequestException("Bad product id provided")

        ProductToGroupModel.deleteWhere { group eq groupId }
        ProductToGroupModel.batchInsert(products) {
            this[group] = groupId
            this[product] = it
        }
    }
}