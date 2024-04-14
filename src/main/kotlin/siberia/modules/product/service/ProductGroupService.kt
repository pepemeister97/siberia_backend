package siberia.modules.product.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.product.data.dao.ProductGroupDao
import siberia.modules.product.data.dto.groups.MassiveUpdateDto
import siberia.modules.product.data.dto.groups.*
import siberia.modules.product.data.dto.groups.systemevents.ProductGroupCreateEvent
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
        authorizedUser: AuthorizedUser,
        productGroupCreateDto: ProductGroupCreateDto
    ): ProductGroupFullOutputDto = transaction {
        val authorDao = UserDao[authorizedUser.id]
        val productGroupDao = ProductGroupDao.new {
            name = productGroupCreateDto.name
        }
        val event = ProductGroupCreateEvent(
            authorDao.login,
            productGroupDao.name,
            productGroupDao.idValue,
        )
        SystemEventModel.logEvent(event)
        ProductToGroupModel.setProducts(productGroupDao.idValue, productGroupCreateDto.products)

        productGroupDao.toFullOutput()
    }

    fun update(
        authorizedUser: AuthorizedUser,
        groupId: Int,
        productGroupUpdateDto: ProductGroupUpdateDto,
        shadowed: Boolean = false,
    ): ProductGroupOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val productGroupDao = ProductGroupDao[groupId]
        productGroupDao.loadAndFlush(userDao.login, productGroupUpdateDto, shadowed)

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
        val productGroupDao = ProductGroupDao[groupId]
        val productGroupDto = productGroupDao.toFullOutput()
        val rollbackList = ProductModel.updateBatch(productGroupDto.products.map { it.id }, productUpdateDto)

        val event = ProductMassiveUpdateEvent(
            author.login,
            groupId,
            productGroupDto.name,
            productGroupDao.getMassiveUpdateRollbackInstance(MassiveUpdateRollbackDto(
                productUpdateDto,
                rollbackList
            ))
        )
        SystemEventModel.logResettableEvent(event)

        ProductGroupActionResultDto(true, "Group of products is updated successfully")
    }
}