package siberia.modules.category.service

import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.category.data.dao.CategoryDao
import siberia.modules.category.data.dto.*
import siberia.modules.category.data.dto.systemevents.CategoryCreateEvent
import siberia.modules.category.data.models.CategoryModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class CategoryService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, categoryInputDto: CategoryInputDto, shadowed: Boolean = false): CategoryOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val parentCategoryId = categoryInputDto.parent ?: 1

        val createdCategory = CategoryModel.new(categoryInputDto, parentCategoryId)

        val event = CategoryCreateEvent(userDao.login, createdCategory.name, createdCategory.id)
        if (!shadowed)
            SystemEventModel.logEvent(event)

        commit()

        createdCategory
    }

    fun remove(authorizedUser: AuthorizedUser, categoryId: Int, categoryOnRemoveDto: CategoryOnRemoveDto): CategoryRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val categoryDao = CategoryDao[categoryId]

        if (categoryDao.idValue == 1)
            throw BadRequestException("You cant remove root category")

        categoryDao.delete(userDao.login, categoryOnRemoveDto)

        commit()

        CategoryRemoveResultDto(
            success = true,
            message = "Category $categoryId successfully removed"
        )
    }

    fun update(authorizedUser: AuthorizedUser, categoryId: Int, categoryUpdateDto: CategoryUpdateDto): CategoryOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val categoryDao = CategoryDao[categoryId]

        CategoryCache.makeInvalid()

        categoryDao.loadAndFlush(userDao.login, categoryUpdateDto)
        commit()

        categoryDao.toOutputDto()
    }

    fun getAll(): List<CategoryOutputDto> {
        return CategoryModel.getFullTree().sortedBy { it.name }
    }

    fun getOne(categoryId: Int): CategoryOutputDto = transaction {
        CategoryDao[categoryId].getWithChildren()
    }
}