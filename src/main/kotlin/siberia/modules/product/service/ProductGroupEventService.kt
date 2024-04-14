package siberia.modules.product.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.product.data.dto.groups.ProductGroupCreateDto
import siberia.modules.product.data.dto.groups.ProductGroupUpdateDto
import siberia.utils.kodein.KodeinEventService

class ProductGroupEventService(di: DI) : KodeinEventService(di) {
    private val productGroupService: ProductGroupService by instance()

    //Massive update rollback
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto): Unit = transaction {
        val rollbackEventData = event.getRollbackData<ProductGroupUpdateDto>()
        productGroupService.update(authorizedUser, rollbackEventData.objectId, rollbackEventData.objectDto, shadowed = true)
    }

    //Product group remove rollback
    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) { transaction {
        val rollbackEventData = event.getRollbackData<ProductGroupCreateDto>()
        productGroupService.create(authorizedUser, rollbackEventData.objectDto)
    } }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
    }
}