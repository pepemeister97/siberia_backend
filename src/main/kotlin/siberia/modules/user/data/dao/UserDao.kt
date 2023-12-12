package siberia.modules.user.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.dto.RoleOutputDto
import siberia.modules.auth.data.models.role.RbacModel
import siberia.modules.user.data.dto.UserOutputDto
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class UserDao(id: EntityID<Int>): BaseIntEntity<UserOutputDto>(id, UserModel) {
    companion object : BaseIntEntityClass<UserOutputDto, UserDao>(UserModel)

    var login by UserModel.login
    var hash by UserModel.hash
    var lastLogin by UserModel.lastLogin

    val rolesWithRules: List<RoleOutputDto>
        get() = RbacModel.userToRoleLinks(idValue, withRules = true, withStock = true)

    val rulesWithStocks: List<LinkedRuleOutputDto>
        get() = RbacModel.userToRuleLinks(userId = idValue, withStock = true)

    override fun toOutputDto(): UserOutputDto =
        UserOutputDto(idValue, login, hash, lastLogin)
}