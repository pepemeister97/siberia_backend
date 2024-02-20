package siberia.modules.product.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.product.data.dto.ProductListItemOutputDto
import siberia.modules.product.data.dto.groups.ProductGroupFullOutputDto
import siberia.modules.product.data.dto.groups.ProductGroupOutputDto
import siberia.modules.product.data.dto.groups.ProductGroupUpdateDto
import siberia.modules.product.data.models.ProductGroupModel
import siberia.modules.product.data.models.ProductToGroupModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class ProductGroupDao(id: EntityID<Int>) : BaseIntEntity<ProductGroupOutputDto>(id, ProductGroupModel) {
    companion object : BaseIntEntityClass<ProductGroupOutputDto, ProductGroupDao>(ProductGroupModel)

    var name by ProductGroupModel.name
    val products: List<ProductListItemOutputDto> get() = ProductToGroupModel.getProducts(idValue)
    override fun toOutputDto(): ProductGroupOutputDto =
        ProductGroupOutputDto(idValue, name)

    fun toFullOutput(): ProductGroupFullOutputDto =
        ProductGroupFullOutputDto(idValue, name, products)

    fun loadAndFlush(authorName: String, productGroupUpdateDto: ProductGroupUpdateDto): Boolean {
        name = productGroupUpdateDto.name ?: name
        if (productGroupUpdateDto.products != null)
            ProductToGroupModel.setProducts(idValue, productGroupUpdateDto.products)

        return flush()
    }

    fun delete(authorName: String) {
        super.delete()
    }
}