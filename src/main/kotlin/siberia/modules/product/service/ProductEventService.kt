package siberia.modules.product.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
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
        productService.update(authorizedUser, updateEventDto.objectId, updateEventDto.objectDto)
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val createProductEvent = event.getRollbackData<ProductRollbackDto>()
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
}