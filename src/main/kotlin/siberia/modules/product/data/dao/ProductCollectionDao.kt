package siberia.modules.product.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.product.data.dto.ProductCollectionOutputDto
import siberia.modules.product.data.models.ProductCollectionModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class ProductCollectionDao(id: EntityID<Int>): BaseIntEntity<ProductCollectionOutputDto>(id, ProductCollectionModel) {
    companion object: BaseIntEntityClass<ProductCollectionOutputDto, ProductCollectionDao>(ProductCollectionModel)

    val name by ProductCollectionModel.name

    override fun toOutputDto(): ProductCollectionOutputDto
        = ProductCollectionOutputDto(idValue, name)


}