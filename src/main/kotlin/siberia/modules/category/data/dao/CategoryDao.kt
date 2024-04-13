package siberia.modules.category.data.dao

import io.ktor.server.plugins.*
import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.category.data.dto.CategoryOnRemoveDto
import siberia.modules.category.data.dto.CategoryOutputDto
import siberia.modules.category.data.dto.CategoryUpdateDto
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
        CategoryOutputDto(idValue, name, parent = CategoryModel.getParent(idValue))

    fun getWithChildren(): CategoryOutputDto =
        toOutputDto().apply { children = CategoryModel.getTreeFrom(this) }

    fun loadAndFlush(authorName: String, categoryUpdateDto: CategoryUpdateDto): Boolean {
        val event = CategoryUpdateEvent(
            authorName,
            with(categoryUpdateDto) {
                if (name == this@CategoryDao.name || name == null) this@CategoryDao.name
                else "$name (${this@CategoryDao.name})"
            },
            idValue,
            createEncodedRollbackUpdateDto<CategoryOutputDto, CategoryUpdateDto>(categoryUpdateDto)
        )
        SystemEventModel.logResettableEvent(event)

        name = categoryUpdateDto.name ?: name

        if (categoryUpdateDto.parent == idValue)
            throw BadRequestException("Bad parent ID")
        if (categoryUpdateDto.parent != null && categoryUpdateDto.parent != 0) {
            val newParent = CategoryDao[categoryUpdateDto.parent!!]
            CategoryModel.moveToNewParent(this, newParent)
        } else if (categoryUpdateDto.parent == 0) {
            val newParent = CategoryDao[1]
            CategoryModel.moveToNewParent(this, newParent)
        }

        return flush()
    }

    fun delete(authorName: String, categoryOnRemoveDto: CategoryOnRemoveDto) {
        val outputWithChildren = getWithChildren()
        outputWithChildren.childrenRemoved = categoryOnRemoveDto.removeChildren
        outputWithChildren.parent = CategoryModel.getParent(idValue)

        val event = CategoryRemoveEvent(
            authorName,
            name,
            idValue,
            createRollbackRemoveDto(outputWithChildren)
        )
        SystemEventModel.logResettableEvent(event)

        if (categoryOnRemoveDto.transferChildrenTo != null) {
            val transferTo = CategoryDao[categoryOnRemoveDto.transferChildrenTo]
            outputWithChildren.children.forEach {
                val category = CategoryDao[it.id]
                CategoryModel.moveToNewParent(category, transferTo)
            }
        }

        CategoryModel.remove(this, categoryOnRemoveDto)

        super.delete()
    }
}