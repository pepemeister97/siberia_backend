package siberia.modules.logger.data.models

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.modules.logger.data.dto.SystemEventTypeOutputDto
import siberia.utils.database.BaseIntIdTable

object SystemEventTypeModel: BaseIntIdTable() {
    val name = text("name")

    fun getAll(): List<SystemEventTypeOutputDto> = transaction {
        selectAll().map { SystemEventTypeOutputDto(id = it[SystemEventTypeModel.id].value, name = it[name]) }
    }
}