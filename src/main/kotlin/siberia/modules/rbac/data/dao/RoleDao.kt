package siberia.modules.rbac.data.dao

import org.jetbrains.exposed.dao.EntityBatchUpdate
import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.rbac.data.dto.RoleUpdateDto
import siberia.modules.rbac.data.dto.systemevents.RoleRemoveEvent
import siberia.modules.rbac.data.dto.systemevents.RoleUpdateEvent
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

    val relatedUsers: List<Pair<Int, String>> get()
    = RbacModel.getRelatedUsers(idValue).map { Pair(it[UserModel.id].value, it[UserModel.name]) }

    val outputWithChildren: RoleOutputDto
        get() {
            return RoleOutputDto(
                idValue, name, description, RbacModel.roleToRuleLinks(idValue), relatedUsers.size.toLong(), relatedUsers
            )
        }

    override fun toOutputDto(): RoleOutputDto
        = RoleOutputDto(idValue, name, description)

    val withRelatedUsers: RoleOutputDto get() {
        return RoleOutputDto(
            idValue, name, description, relatedUsersCount = relatedUsers.size.toLong()
        )
    }

    fun loadAndFlush(authorName: String, roleUpdateDto: RoleUpdateDto, batch: EntityBatchUpdate? = null): Boolean {
        val event = RoleUpdateEvent(
            authorName,
            name,
            roleUpdateDto.name ?: name,
            createRollbackUpdateDto<RoleOutputDto, RoleUpdateDto>(roleUpdateDto),
            idValue
        )
        SystemEventModel.logResettableEvent(event)

        if (roleUpdateDto.name != null)
            name = roleUpdateDto.name!!

        if (roleUpdateDto.description != null)
            description = roleUpdateDto.description

        return super.flush(batch)
    }

    fun delete(authorName: String) {
        val event = RoleRemoveEvent(
            authorName,
            name,
            createRollbackRemoveDto<RoleOutputDto>(outputWithChildren),
            idValue
        )

        SystemEventModel.logResettableEvent(event)

        super.delete()
    }
}