package siberia.modules.product.service

import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.product.data.dao.ProductDao
import siberia.modules.product.data.dto.ProductMassiveInsertRollbackDto
import siberia.modules.product.data.dto.groups.MassiveUpdateRollbackDto
import siberia.modules.product.data.models.ProductModel
import siberia.utils.kodein.KodeinEventService

class ProductMassiveEventService(di: DI) : KodeinEventService(di) {
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val rollbackMassiveUpdate = event.getRollbackData<MassiveUpdateRollbackDto>().objectDto
        rollbackMassiveUpdate.productsData.forEach {
            val product = ProductDao[it.first.id!!]
            product.loadUpdateDto(it.first)
            product.flush()
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto): Unit = transaction {
        val productMassiveInsertRollbackDto = event.getRollbackData<ProductMassiveInsertRollbackDto>()
        ProductModel.deleteWhere {
            ProductModel.id inList productMassiveInsertRollbackDto.objectDto.productsList.map { it.id }
        }
    }
}