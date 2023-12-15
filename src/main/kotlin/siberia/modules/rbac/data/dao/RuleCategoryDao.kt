package siberia.modules.rbac.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.rbac.data.dto.RuleCategoryOutputDto
import siberia.modules.rbac.data.models.rule.RuleCategoryModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class RuleCategoryDao(id: EntityID<Int>): BaseIntEntity<RuleCategoryOutputDto>(id, RuleCategoryModel) {

    companion object: BaseIntEntityClass<RuleCategoryOutputDto, RuleCategoryDao>(RuleCategoryModel)

    val name by RuleCategoryModel.name

    override fun toOutputDto(): RuleCategoryOutputDto
        = RuleCategoryOutputDto(idValue, name)
}