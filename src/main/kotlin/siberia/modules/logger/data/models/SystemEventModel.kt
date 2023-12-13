package siberia.modules.logger.data.models

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.utils.database.BaseIntIdTable

object SystemEventModel: BaseIntIdTable() {
    val eventType = reference("event_type", SystemEventTypeModel, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
    val author = text("author")
    val description = text("description")

    fun getList(query: SqlExpressionBuilder.() -> Op<Boolean>): List<SystemEventOutputDto> = transaction {
        SystemEventModel
            .leftJoin(SystemEventTypeModel)
            .slice(SystemEventModel.id, SystemEventTypeModel.name, author, description, createdAt)
            .select(query)
            .map {
                SystemEventOutputDto(
                    id = it[SystemEventModel.id].value,
                    author = it[author],
                    eventType = it[SystemEventTypeModel.name],
                    description = it[description],
                    timestamp = it[createdAt].toString()
                )
            }
    }
}