package siberia.modules.category.data.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.modules.category.data.dao.CategoryDao
import siberia.modules.category.data.dto.CategoryInputDto
import siberia.modules.category.data.dto.CategoryOnRemoveDto
import siberia.modules.category.data.dto.CategoryOutputDto
import siberia.utils.database.BaseIntIdTable
import siberia.utils.database.idValue

object CategoryModel: BaseIntIdTable() {
    val name = text("name")

    fun new(categoryInputDto: CategoryInputDto, parentId: Int): CategoryOutputDto = transaction {
        val category = CategoryDao.wrapRow(CategoryModel.insert {
            it[name] = categoryInputDto.name
        }.resultedValues!!.first())

        CategoryToCategoryModel.insert {
            it[parent] = parentId
            it[child] = category.id
        }

        category.toOutputDto()
    }

    fun getTreeFrom(categoryOutputDto: CategoryOutputDto): List<CategoryOutputDto> = transaction {
        CategoryToCategoryModel
            .join(CategoryModel, JoinType.LEFT, CategoryToCategoryModel.child, CategoryModel.id)
            .slice(CategoryModel.id, name, CategoryToCategoryModel.child)
            .select {
                CategoryToCategoryModel.parent eq categoryOutputDto.id
            }.map {
                CategoryOutputDto(
                    id = it[CategoryModel.id].value, name = it[name]
                ).apply {
                    children = getTreeFrom(this)
                }
            }
    }

    private fun getIdsFromTree(categoryOutputDto: CategoryOutputDto, ids: MutableList<Int>) {
        ids.add(categoryOutputDto.id)
        categoryOutputDto.children.forEach { getIdsFromTree(it, ids) }
    }

    fun getFullTree(): List<CategoryOutputDto> = transaction {
        val categoryDao = CategoryDao[1]
        getTreeFrom(categoryDao.toOutputDto())
    }

    fun remove(categoryDao: CategoryDao, categoryOnRemoveDto: CategoryOnRemoveDto) {
        val ids = mutableListOf(categoryDao.idValue)

        //If marked as removeChild we need to remove full tree of children
        if (categoryOnRemoveDto.removeChildren) {
            getTreeFrom(categoryDao.toOutputDto()).forEach {
                getIdsFromTree(it, ids)
            }
        // We check if transferChildrenTo param is provided -> transfer children to this category,
        // if not -> look for removed category parent and move children to that category
        } else {
            val parentOfRemovedCategory = categoryOnRemoveDto.transferChildrenTo ?: CategoryToCategoryModel.select {
                CategoryToCategoryModel.child eq categoryDao.id
            }.first()[CategoryToCategoryModel.parent].value

            CategoryToCategoryModel.update({ CategoryToCategoryModel.parent eq categoryDao.id }) {
                it[parent] = parentOfRemovedCategory
            }
        }

        CategoryModel.deleteWhere {
            id inList ids
        }
    }

    fun moveToNewParent(categoryDao: CategoryDao, newParent: CategoryDao) {
        CategoryToCategoryModel.update( { CategoryToCategoryModel.child eq categoryDao.id } ) {
            it[parent] = newParent.id
        }
    }
}