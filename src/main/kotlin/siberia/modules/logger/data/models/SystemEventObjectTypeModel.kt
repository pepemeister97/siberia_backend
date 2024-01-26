package siberia.modules.logger.data.models

import org.jetbrains.exposed.sql.selectAll
import siberia.modules.logger.data.dto.SystemEventObjectTypeOutputDto
import siberia.utils.database.BaseIntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object SystemEventObjectTypeModel: BaseIntIdTable() {
    val name = text("name")

    fun getAll(): List<SystemEventObjectTypeOutputDto> = transaction {
        selectAll().map { SystemEventObjectTypeOutputDto(id = it[SystemEventObjectTypeModel.id].value, name = it[name]) }
    }
}