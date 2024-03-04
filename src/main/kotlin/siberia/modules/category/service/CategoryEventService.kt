package siberia.modules.category.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.category.data.dto.CategoryOutputDto
import siberia.modules.category.data.dto.CategoryUpdateDto
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.utils.kodein.KodeinEventService

class CategoryEventService(di: DI) : KodeinEventService(di) {
    private val categoryService: CategoryService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val updateEventDto = event.getRollbackData<CategoryUpdateDto>()
        categoryService.update(authorizedUser, updateEventDto.objectId, updateEventDto.objectDto)
    }

    private fun createRecursive(authorizedUser: AuthorizedUser, categories: List<CategoryOutputDto>, parent: Int) {
        categories.forEach {
            val createDto = it.createDto
            createDto.parent = parent
            val category = categoryService.create(authorizedUser, createDto)
            if (it.children.isNotEmpty())
                createRecursive(authorizedUser, it.children, category.id)
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val createEventDto = event.getRollbackData<CategoryOutputDto>()
        val categoryDto = categoryService.create(authorizedUser, createEventDto.objectDto.createDto)
        if (createEventDto.objectDto.childrenRemoved) {
            createRecursive(authorizedUser, createEventDto.objectDto.children, categoryDto.parent)
        }
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}