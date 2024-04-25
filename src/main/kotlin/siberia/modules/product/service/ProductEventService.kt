package siberia.modules.product.service

import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.brand.data.dao.BrandDao
import siberia.modules.category.data.dao.CategoryDao
import siberia.modules.collection.data.dao.CollectionDao
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.product.data.dao.ProductDao
import siberia.modules.product.data.dto.ProductRollbackDto
import siberia.modules.product.data.dto.ProductUpdateDto
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.utils.kodein.KodeinEventService

class ProductEventService(di: DI) : KodeinEventService(di) {
    private val productService: ProductService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val updateEventDto = event.getRollbackData<ProductUpdateDto>()
        try {
            ProductDao[updateEventDto.objectId]
            productService.update(authorizedUser, updateEventDto.objectId, updateEventDto.objectDto)
        } catch (_: EntityNotFoundException) {}
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val createProductEvent = event.getRollbackData<ProductRollbackDto>()
        createProductEvent.objectDto.brand = try {
            BrandDao[createProductEvent.objectDto.brand ?: 0]
            createProductEvent.objectDto.brand
        } catch (_: EntityNotFoundException) {
            null
        }
        createProductEvent.objectDto.collection = try {
            CollectionDao[createProductEvent.objectDto.collection ?: 0]
            createProductEvent.objectDto.collection
        } catch (_: EntityNotFoundException) {
            null
        }
        createProductEvent.objectDto.category = try {
            CategoryDao[createProductEvent.objectDto.category ?: 0]
            createProductEvent.objectDto.category
        } catch (_: EntityNotFoundException) {
            null
        }
        val productDto = productService.create(authorizedUser, createProductEvent.objectDto.createDto)
        val productDao = ProductDao[productDto.id]
        productDao.lastPurchaseDate = createProductEvent.objectDto.lastPurchaseDate
        productDao.lastPurchasePrice = createProductEvent.objectDto.lastPurchasePrice
        productDao.cost = createProductEvent.objectDto.cost
        createProductEvent.objectDto.stocksRelations.forEach {
            StockModel.appendProducts(it.key, it.value.map { product ->
                TransactionInputDto.TransactionProductInputDto(
                    product.key, product.value.first, product.value.second
                )
            })
        }
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}