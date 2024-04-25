package siberia.modules.stock.service

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.auth.service.AuthSocketService
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.product.data.models.ProductModel
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.rbac.service.RbacService
import siberia.modules.stock.data.dto.StockRollbackRemoveDto
import siberia.modules.stock.data.dto.StockUpdateDto
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.user.data.models.UserModel
import siberia.modules.user.service.UserAccessControlService
import siberia.utils.kodein.KodeinEventService

class StockEventService(di: DI) : KodeinEventService(di) {
    private val stockService: StockService by instance()
    private val rbacService: RbacService by instance()
    private val userAccessControlService: UserAccessControlService by instance()
    private val authSocketService: AuthSocketService by instance()

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

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto): Unit = transaction {
        val createEventDto = event.getRollbackData<StockRollbackRemoveDto>()
        val stockDto = stockService.create(authorizedUser, createEventDto.objectDto.createDto, autoCommit = false)

        val listOfExistProducts = ProductModel.slice(ProductModel.id).select {
            ProductModel.id inList createEventDto.objectDto.products.map { it.id }
        }.map {
            it[ProductModel.id].value
        }

        UserModel.slice(UserModel.id).select {
            UserModel.id inList createEventDto.objectDto.relatedUsers.keys
        }.forEach {
            val userId = it[UserModel.id].value
            userAccessControlService.addRules(authorizedUser, userId, createEventDto.objectDto.getRulesRelatedToUser(stockDto.id, userId), shadowed = true)
        }

        RoleModel.slice(RoleModel.id).select {
            RoleModel.id inList createEventDto.objectDto.relatedRoles.keys
        }.forEach {
            val roleId = it[RoleModel.id].value
            rbacService.appendRulesToRole(authorizedUser, roleId, createEventDto.objectDto.getRulesRelatedToRole(stockDto.id, roleId), needLog = false, autoCommit = false)
        }


        StockModel.appendProducts(stockDto.id, createEventDto.objectDto.products.
            filter {
                listOfExistProducts.contains(it.id)
            }
            .map {
            TransactionInputDto.TransactionProductInputDto(
                it.id, it.quantity, it.price
            )
        })

        authSocketService.updateRules(RbacModel.getUsersRelatedToStock(stockDto.id))
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}