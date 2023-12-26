package siberia.modules.logger.data.models

import org.jetbrains.exposed.sql.*
import siberia.utils.database.transaction
import siberia.modules.logger.data.dto.SystemEventCreateDto
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.utils.database.BaseIntIdTable

object SystemEventModel: BaseIntIdTable() {
    val author = text("author")
    val eventObjectName = text("event_object_name")
    val eventObjectType = reference("event_object_type", SystemEventObjectTypeModel, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
    val eventType = reference("event_type", SystemEventTypeModel, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
    val eventDescription = text("event_description")

    //Next iterations
//    val eventObject = integer("event_object")
//    val objectBefore = text("objectBefore")
//    val objectAfter = text("objectAfter")
//    val relatedTo = reference("related_to", SystemEventModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
//    val canBeRestored = bool("can_be_restored")

    fun getList(query: SqlExpressionBuilder.() -> Op<Boolean>): List<SystemEventOutputDto<*>> = transaction {
        SystemEventModel
            .leftJoin(SystemEventTypeModel)
            .leftJoin(SystemEventObjectTypeModel)
            .slice(
                SystemEventModel.id,
                author,
                SystemEventTypeModel.name,
                eventObjectName,
                eventObjectType,
                eventDescription,
                SystemEventObjectTypeModel.name,
                createdAt
            )
            .select(query)
            .map {
                SystemEventOutputDto<Any>(
                    id = it[SystemEventModel.id].value,
                    author = it[author],
                    eventType = it[SystemEventTypeModel.name],
                    eventObjectName = it[eventObjectName],
                    eventObjectType = it[SystemEventObjectTypeModel.name],
                    eventDescription = it[eventDescription],
                    timestamp = it[createdAt].toString()
                )
            }
    }

    fun <T : SystemEventCreateDto> logEvent(event: T) = transaction {
        SystemEventModel.insert {
            it[author] = event.author
            it[eventObjectName] = event.eventObjectName
            it[eventObjectType] = event.eventObjectType
            it[eventDescription] = event.eventDescription
            it[eventType] = event.eventType
        }
    }
}