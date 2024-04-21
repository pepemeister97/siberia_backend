package siberia.modules.category.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.category.data.dto.CategoryOutputDto
import siberia.modules.category.data.dto.CategoryUpdateDto
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.plugins.Logger
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
            val category = categoryService.create(authorizedUser, createDto, shadowed = true)
            Logger.debug("create sub for $parent: $category", "main")
            if (it.children.isNotEmpty())
                Logger.debug("start creating children ${it.children}", "main")
                createRecursive(authorizedUser, it.children, category.id)
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val createEventDto = event.getRollbackData<CategoryOutputDto>()
        val categoryDto = categoryService.create(authorizedUser, createEventDto.objectDto.createDto, shadowed = true)
        Logger.debug("create main: $categoryDto", "main")
        if (createEventDto.objectDto.childrenRemoved) {
            Logger.debug("start creating children ${createEventDto.objectDto.children}", "main")
            createRecursive(authorizedUser, createEventDto.objectDto.children, categoryDto.id)
        }
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}