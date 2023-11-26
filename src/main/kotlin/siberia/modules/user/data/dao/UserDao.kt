package siberia.modules.user.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.models.rule.UserToRuleModel
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

    val rules: List<LinkedRuleOutputDto>
        get() = transaction {
            println("UserDao id = $idValue")
            UserToRuleModel
                .slice(UserToRuleModel.rule, UserToRuleModel.stock)
                .select {
                    UserToRuleModel.user eq idValue
                }.map {
                    println("Link ${it}")
                    LinkedRuleOutputDto(
                        ruleId = it[UserToRuleModel.rule].value,
                        stockId = it[UserToRuleModel.stock]?.value
                    )
                }
        }

    override fun toOutputDto(): UserOutputDto =
        UserOutputDto(idValue, login, hash, lastLogin)
}