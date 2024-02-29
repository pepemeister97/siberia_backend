package siberia.modules.product.service

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.product.data.dao.ProductGroupDao
import siberia.modules.product.data.dto.groups.MassiveUpdateDto
import siberia.modules.product.data.dto.groups.*
import siberia.modules.product.data.dto.groups.systemevents.ProductMassiveUpdateEvent
import siberia.modules.product.data.models.ProductModel
import siberia.modules.product.data.models.ProductToGroupModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class ProductGroupService(di: DI) : KodeinService(di) {
    fun getAll(): List<ProductGroupOutputDto> = transaction {
        ProductGroupDao.all().map { it.toOutputDto() }
    }

    fun getOne(groupId: Int): ProductGroupFullOutputDto = transaction {
        ProductGroupDao[groupId].toFullOutput()
    }

    fun create(
        productGroupCreateDto: ProductGroupCreateDto
    ): ProductGroupFullOutputDto = transaction {
        val productGroupDao = ProductGroupDao.new {
            name = productGroupCreateDto.name
        }
        ProductToGroupModel.setProducts(productGroupDao.idValue, productGroupCreateDto.products)

        productGroupDao.toFullOutput()
    }

    fun update(
        groupId: Int,
        productGroupUpdateDto: ProductGroupUpdateDto
    ): ProductGroupOutputDto = transaction {
        val productGroupDao = ProductGroupDao[groupId]
        productGroupDao.loadAndFlush(productGroupUpdateDto)

        productGroupDao.toOutputDto()
    }

    fun remove(authorizedUser: AuthorizedUser, groupId: Int): ProductGroupActionResultDto = transaction {
        val author = UserDao[authorizedUser.id]
        val productGroupDao = ProductGroupDao[groupId]
        productGroupDao.delete(author.login)

        ProductGroupActionResultDto(true, "Group is removed successfully")
    }

    fun updateGroup(
        authorizedUser: AuthorizedUser,
        groupId: Int,
        productUpdateDto: MassiveUpdateDto
    ): ProductGroupActionResultDto = transaction {
        val author = UserDao[authorizedUser.id]
        val productGroupDto = ProductGroupDao[groupId].toFullOutput()
        val rollbackList = ProductModel.updateBatch(productGroupDto.products.map { it.id }, productUpdateDto)

        val event = ProductMassiveUpdateEvent(
            author.login,
            groupId,
            productGroupDto.name,
            Json.encodeToString(MassiveUpdateRollbackDto.serializer(), MassiveUpdateRollbackDto(rollbackList))
        )
        SystemEventModel.logResettableEvent(event)

        ProductGroupActionResultDto(true, "Group of products is updated successfully")
    }
}