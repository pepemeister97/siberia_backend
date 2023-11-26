package siberia.modules.product.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.product.data.dto.ProductOutputDto
import siberia.modules.product.data.models.ProductModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class ProductDao(id: EntityID<Int>): BaseIntEntity<ProductOutputDto>(id, ProductModel) {

    companion object: BaseIntEntityClass<ProductOutputDto, ProductDao>(ProductModel)

    val name by ProductModel.name
    val price by ProductModel.price

    override fun toOutputDto(): ProductOutputDto
        = ProductOutputDto(idValue, name, price)
}