package siberia.modules.category.data.dao

import org.jetbrains.exposed.dao.EntityBatchUpdate
import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.category.data.dto.CategoryOutputDto
import siberia.modules.category.data.dto.systemevents.CategoryRemoveEvent
import siberia.modules.category.data.dto.systemevents.CategoryUpdateEvent
import siberia.modules.category.data.models.CategoryModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class CategoryDao(id: EntityID<Int>) : BaseIntEntity<CategoryOutputDto>(id, CategoryModel) {

    companion object : BaseIntEntityClass<CategoryOutputDto, CategoryDao>(CategoryModel)

    var name by CategoryModel.name
    override fun toOutputDto(): CategoryOutputDto =
        CategoryOutputDto(idValue, name)

    fun getWithChildren(): CategoryOutputDto =
        toOutputDto().apply { children = CategoryModel.getTreeFrom(this) }

    fun flush(authorName: String, batch: EntityBatchUpdate? = null): Boolean {
        val event = CategoryUpdateEvent(authorName, name)
        SystemEventModel.logEvent(event)
        return super.flush(batch)
    }

    fun delete(authorName: String) {
        val event = CategoryRemoveEvent(authorName, name)
        SystemEventModel.logEvent(event)
        super.delete()
    }
}