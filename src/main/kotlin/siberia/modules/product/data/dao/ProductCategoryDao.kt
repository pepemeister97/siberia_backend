package siberia.modules.product.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.product.data.dto.ProductCategoryOutputDto
import siberia.modules.product.data.models.ProductCategoryModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class ProductCategoryDao(id: EntityID<Int>): BaseIntEntity<ProductCategoryOutputDto>(id, ProductCategoryModel) {

    companion object: BaseIntEntityClass<ProductCategoryOutputDto, ProductCategoryDao>(ProductCategoryModel)

    val name by ProductCategoryModel.name

    override fun toOutputDto(): ProductCategoryOutputDto
        = ProductCategoryOutputDto(idValue, name)
}