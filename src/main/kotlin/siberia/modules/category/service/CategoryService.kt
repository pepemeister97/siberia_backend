package siberia.modules.category.service

import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.category.data.dao.CategoryDao
import siberia.modules.category.data.dto.CategoryInputDto
import siberia.modules.category.data.dto.CategoryOnRemoveDto
import siberia.modules.category.data.dto.CategoryOutputDto
import siberia.modules.category.data.dto.CategoryRemoveResultDto
import siberia.modules.category.data.dto.systemevents.CategoryCreateEvent
import siberia.modules.category.data.dto.systemevents.CategoryRemoveEvent
import siberia.modules.category.data.models.CategoryModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class CategoryService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, categoryInputDto: CategoryInputDto): CategoryOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val parentCategoryId = categoryInputDto.parent ?: 1

        val createdCategory = CategoryModel.new(categoryInputDto, parentCategoryId)

        val event = CategoryCreateEvent(userDao.login, createdCategory.name)
        SystemEventModel.logEvent(event)

        createdCategory
    }

    fun remove(authorizedUser: AuthorizedUser, categoryId: Int, categoryOnRemoveDto: CategoryOnRemoveDto): CategoryRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val categoryDao = CategoryDao[categoryId]

        if (categoryDao.idValue == 1)
            throw BadRequestException("You cant remove root category")

        if (categoryOnRemoveDto.transferChildrenTo != null)
            CategoryDao[categoryOnRemoveDto.transferChildrenTo]

        CategoryModel.remove(categoryDao, categoryOnRemoveDto)

        val event = CategoryRemoveEvent(userDao.login, categoryDao.name)
        SystemEventModel.logEvent(event)

        CategoryRemoveResultDto(
            success = true,
            message = "Category $categoryId successfully removed"
        )
    }

    fun update(authorizedUser: AuthorizedUser, categoryId: Int, categoryInputDto: CategoryInputDto): CategoryOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val categoryDao = CategoryDao[categoryId]
        categoryDao.name = categoryInputDto.name
        if (categoryInputDto.parent != null) {
            val newParent = CategoryDao[categoryInputDto.parent]
            CategoryModel.moveToNewParent(categoryDao, newParent)
        }
        categoryDao.flush(userDao.login)
        categoryDao.toOutputDto()
    }

    fun getAll(): List<CategoryOutputDto> {
        return CategoryModel.getFullTree()
    }

    fun getOne(categoryId: Int): CategoryOutputDto = transaction {
        CategoryDao[categoryId].getWithChildren()
    }
}