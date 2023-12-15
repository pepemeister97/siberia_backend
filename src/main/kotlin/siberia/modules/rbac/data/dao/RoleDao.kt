package siberia.modules.rbac.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class RoleDao(id: EntityID<Int>): BaseIntEntity<RoleOutputDto>(id, RoleModel) {
    companion object: BaseIntEntityClass<RoleOutputDto, RoleDao>(RoleModel)

    var name by RoleModel.name

    val outputWithChildren
        get() = RoleOutputDto(
            idValue, name, RbacModel.roleToRuleLinks(idValue)
        )

    override fun toOutputDto(): RoleOutputDto
        = RoleOutputDto(idValue, name)
}