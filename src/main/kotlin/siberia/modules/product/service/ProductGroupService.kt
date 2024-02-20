package siberia.modules.product.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.product.data.dao.ProductGroupDao
import siberia.modules.product.data.dto.ProductMassiveUpdateDto
import siberia.modules.product.data.dto.groups.*
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
        val productGroupDao = ProductGroupDao.new {
            name = productGroupCreateDto.name
        }
        ProductToGroupModel.setProducts(productGroupDao.idValue, productGroupCreateDto.products)

        productGroupDao.toFullOutput()
    }

    fun update(
        authorizedUser: AuthorizedUser,
        groupId: Int,
        productGroupUpdateDto: ProductGroupUpdateDto
    ): ProductGroupOutputDto = transaction {
        val author = UserDao[authorizedUser.id]
        val productGroupDao = ProductGroupDao[groupId]
        productGroupDao.loadAndFlush(author.login, productGroupUpdateDto)

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
        productUpdateDto: ProductMassiveUpdateDto
    ): ProductGroupActionResultDto = transaction {
        val author = UserDao[authorizedUser.id]
        val productGroupDto = ProductGroupDao[groupId].toFullOutput()
        ProductModel.updateBatch(productGroupDto.products.map { it.id }, productUpdateDto)

        ProductGroupActionResultDto(true, "Group of products is updated successfully")
    }
}