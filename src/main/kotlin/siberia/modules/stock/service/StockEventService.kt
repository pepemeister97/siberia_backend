package siberia.modules.stock.service

import org.jetbrains.exposed.sql.select
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.product.data.models.ProductModel
import siberia.modules.stock.data.dto.StockFullOutputDto
import siberia.modules.stock.data.dto.StockUpdateDto
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.utils.kodein.KodeinEventService

class StockEventService(di: DI) : KodeinEventService(di) {
    private val stockService: StockService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val updateEventDto = event.getRollbackData<StockUpdateDto>()
        with (StockModel.select {
            StockModel.id eq updateEventDto.objectId
        }.map {
            it[StockModel.id]
        }) {
            if (this.isNotEmpty())
                stockService.update(authorizedUser, updateEventDto.objectId, updateEventDto.objectDto)
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val createEventDto = event.getRollbackData<StockFullOutputDto>()
        val stockDto = stockService.create(authorizedUser, createEventDto.objectDto.createDto)

        val listOfExisting = ProductModel.select {
            ProductModel.id inList createEventDto.objectDto.products.map { it.id }
        }.map {
            it[ProductModel.id].value
        }

        StockModel.appendProducts(stockDto.id, createEventDto.objectDto.products.
            filter {
                listOfExisting.contains(it.id)
            }
            .map {
            TransactionInputDto.TransactionProductInputDto(
                it.id, it.quantity, it.price
            )
        })
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}