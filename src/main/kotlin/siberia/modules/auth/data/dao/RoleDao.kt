package siberia.modules.auth.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.modules.auth.data.dto.RoleOutputDto
import siberia.modules.auth.data.models.role.RoleModel
import siberia.modules.auth.data.models.role.RoleToRuleModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class RoleDao(id: EntityID<Int>): BaseIntEntity<RoleOutputDto>(id, RoleModel) {
    companion object: BaseIntEntityClass<RoleOutputDto, RoleDao>(RoleModel)

    val name by RoleModel.name

    val outputWithChildren: RoleOutputDto
        get() = transaction {
            val linkedRules = RoleToRuleModel.select {
                RoleToRuleModel.role eq idValue
            }.map {
                RoleToRuleDao.wrapRow(it).toOutputDto()
            }.toMutableList()

            RoleOutputDto(idValue, name, linkedRules)
        }

    override fun toOutputDto(): RoleOutputDto
        = RoleOutputDto(idValue, name)
}