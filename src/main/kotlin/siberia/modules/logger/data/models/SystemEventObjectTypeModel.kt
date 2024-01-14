package siberia.modules.logger.data.models

import org.jetbrains.exposed.sql.selectAll
import siberia.modules.logger.data.dto.SystemEventObjectTypeOutputDto
import siberia.utils.database.BaseIntIdTable
import siberia.utils.database.transaction

object SystemEventObjectTypeModel: BaseIntIdTable() {
    val name = text("name")

    fun getAll(): List<SystemEventObjectTypeOutputDto> = transaction {
        selectAll().map { SystemEventObjectTypeOutputDto(id = it[SystemEventObjectTypeModel.id].value, name = it[name]) }
    }
}