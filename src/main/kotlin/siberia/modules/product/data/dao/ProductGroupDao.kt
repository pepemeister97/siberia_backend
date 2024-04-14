package siberia.modules.product.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.product.data.dto.ProductListItemOutputDto
import siberia.modules.product.data.dto.groups.*
import siberia.modules.product.data.dto.groups.systemevents.ProductGroupRemoveEvent
import siberia.modules.product.data.dto.groups.systemevents.ProductGroupUpdateEvent
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

    private fun removeRollbackDto(): ProductGroupCreateDto =
        ProductGroupCreateDto(name, products.map { it.id })

    private fun updateRollbackDto(): ProductGroupUpdateRollbackDto =
        ProductGroupUpdateRollbackDto(name, products.map { it.id })

    fun loadAndFlush(authorName: String, productGroupUpdateDto: ProductGroupUpdateDto, shadowed: Boolean = false): Boolean {
        if (!shadowed) {
            val event = ProductGroupUpdateEvent(
                authorName,
                with(productGroupUpdateDto) {
                    if (name == this@ProductGroupDao.name || name == null) this@ProductGroupDao.name
                    else "$name (${this@ProductGroupDao.name})"
                },
                idValue,
                createEncodedRollbackUpdateDto<ProductGroupUpdateRollbackDto, ProductGroupUpdateDto>(productGroupUpdateDto, updateRollbackDto())
            )

            SystemEventModel.logResettableEvent(event)
        }

        name = productGroupUpdateDto.name ?: name
        if (productGroupUpdateDto.products != null)
            ProductToGroupModel.setProducts(idValue, productGroupUpdateDto.products ?: listOf())


        return flush()
    }

    fun delete(authorName: String) {
        val event = ProductGroupRemoveEvent(
            authorName,
            name,
            idValue,
            createRollbackRemoveDto<ProductGroupCreateDto>(removeRollbackDto())
        )
        SystemEventModel.logResettableEvent(event)

        super.delete()
    }

    fun getMassiveUpdateRollbackInstance(massiveUpdateRollbackDto: MassiveUpdateRollbackDto): String =
        createRollbackRemoveDto<MassiveUpdateRollbackDto>(massiveUpdateRollbackDto)

}