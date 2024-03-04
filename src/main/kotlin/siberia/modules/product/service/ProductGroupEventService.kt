package siberia.modules.product.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.product.data.dao.ProductDao
import siberia.modules.product.data.dto.groups.MassiveUpdateRollbackDto
import siberia.modules.product.data.dto.groups.ProductGroupCreateDto
import siberia.utils.kodein.KodeinEventService

class ProductGroupEventService(di: DI) : KodeinEventService(di) {
    private val productGroupService: ProductGroupService by instance()

    //Massive update rollback
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val rollbackMassiveUpdate = event.getRollbackData<MassiveUpdateRollbackDto>().objectDto
        rollbackMassiveUpdate.productsData.forEach {
            val product = ProductDao[it.id!!]
            product.loadUpdateDto(it)
            product.flush()
        }
    }

    //Product group remove rollback
    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) { transaction {
        val rollbackEventData = event.getRollbackData<ProductGroupCreateDto>()
        productGroupService.create(rollbackEventData.objectDto)
    } }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
    }
}