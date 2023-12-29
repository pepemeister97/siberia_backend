package siberia.modules.rbac.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.rbac.data.dto.RoleUpdateDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class RoleDao(id: EntityID<Int>): BaseIntEntity<RoleOutputDto>(id, RoleModel) {
    companion object: BaseIntEntityClass<RoleOutputDto, RoleDao>(RoleModel)

    var name by RoleModel.name
    var description by RoleModel.description

    val outputWithChildren: RoleOutputDto
        get() {
            val relatedUsers = RbacModel.getRelatedUsers(idValue).map { it[UserModel.name] }
            return RoleOutputDto(
                idValue, name, description, RbacModel.roleToRuleLinks(idValue), relatedUsers.size.toLong(), relatedUsers
            )
        }

    override fun toOutputDto(): RoleOutputDto
        = RoleOutputDto(idValue, name, description)

    val withRelatedUsers: RoleOutputDto get() {
        val relatedUsers = RbacModel.getRelatedUsers(idValue).count()
        return RoleOutputDto(
            idValue, name, description, relatedUsersCount = relatedUsers
        )
    }

    fun loadUpdateDto(roleUpdateDto: RoleUpdateDto) {
        if (roleUpdateDto.name != null)
            name = roleUpdateDto.name

        if (roleUpdateDto.description != null)
            description = roleUpdateDto.description
    }
}