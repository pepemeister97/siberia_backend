package siberia.modules.product.service

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.product.data.dto.groups.ProductGroupCreateDto
import siberia.modules.product.data.dto.groups.ProductGroupUpdateDto
import siberia.modules.product.data.models.ProductGroupModel
import siberia.modules.product.data.models.ProductModel
import siberia.utils.kodein.KodeinEventService

class ProductGroupEventService(di: DI) : KodeinEventService(di) {
    private val productGroupService: ProductGroupService by instance()

    //Massive update rollback
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto): Unit = transaction {
        val rollbackEventData = event.getRollbackData<ProductGroupUpdateDto>()
        with(
            ProductGroupModel.slice(ProductGroupModel.id).select {
                ProductGroupModel.id eq rollbackEventData.objectId
            }
            .map {
                it[ProductGroupModel.id]
            }
        ){
            if (this.isNotEmpty())
                productGroupService.update(authorizedUser, rollbackEventData.objectId, rollbackEventData.objectDto, shadowed = true)
            else throw BadRequestException("rollback failed model removed")
        }
    }

    //Product group remove rollback
    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) : Unit =  transaction {
        val rollbackEventData = event.getRollbackData<ProductGroupCreateDto>()

        rollbackEventData.objectDto.products = ProductModel.slice(ProductModel.id).select {
            ProductModel.id inList rollbackEventData.objectDto.products
        }.map {
            it[ProductModel.id].value
        }
        productGroupService.create(authorizedUser, rollbackEventData.objectDto)
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
    }
}