package siberia.modules.logger.data.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.exceptions.NotFoundException
import siberia.modules.logger.data.dto.SystemEventCreateDto
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.logger.data.dto.resettable.ResettableSystemEventCreateDto
import siberia.utils.database.BaseIntIdTable

object SystemEventModel: BaseIntIdTable() {
    val author = text("author")
    val eventObjectName = text("event_object_name")
    val eventObjectType = reference("event_object_type", SystemEventObjectTypeModel, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
    val eventType = reference("event_type", SystemEventTypeModel, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
    val eventDescription = text("event_description")
    val rollbackRoute = text("rollback_route").nullable().default(null)

    //Updated object id
    val eventObjectId = integer("event_object").nullable().default(null)
    //CreateDto \ UpdateDto json for object
    val rollbackInstance = text("rollback_instance").nullable().default(null)
    /*
        For complex events
        For example Products removing
        it leads to remove products from storehouses, transactions etc.
        such side events mark with related_to field which points to main event (product removing in this case)
     */
    val relatedTo = reference("related_to", SystemEventModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable().default(null)
    val canBeReset = bool("can_be_reset")

    private fun ResultRow.toOutputDto(needRollback: Boolean = false): SystemEventOutputDto {

        val systemEventOutputDto = SystemEventOutputDto(
            id = this[SystemEventModel.id].value,
            author = this[author],
            eventType = this[SystemEventTypeModel.name],
            eventObjectName = this[eventObjectName],
            eventObjectType = this[SystemEventObjectTypeModel.name],
            eventDescription = this[eventDescription],
            eventObjectId = this[eventObjectId],
            timestamp = this[createdAt].toString(),
            eventObjectTypeId = this[eventObjectType].value,
            eventTypeId = this[eventType].value,
            canBeReset = this[canBeReset],
        )

        if (needRollback) {
            systemEventOutputDto.rollbackInstance = this[rollbackInstance]
            systemEventOutputDto.rollbackRoute = this[rollbackRoute]
        }

        return systemEventOutputDto
    }

    private fun Join.sliceForOutput(needRollback: Boolean = false) = with(this) {
        val slice = mutableListOf(
            SystemEventModel.id,
            author,
            eventType,
            SystemEventTypeModel.name,
            eventObjectName,
            eventObjectType,
            eventDescription,
            eventObjectId,
            SystemEventObjectTypeModel.name,
            createdAt,
            canBeReset
        )

        if (needRollback) {
            slice.add(rollbackInstance)
            slice.add(rollbackRoute)
        }

        slice(slice)
    }

    fun getList(query: SqlExpressionBuilder.() -> Op<Boolean>): List<SystemEventOutputDto> = transaction {
        SystemEventModel
            .leftJoin(SystemEventTypeModel)
            .leftJoin(SystemEventObjectTypeModel)
            .sliceForOutput()
            .select(query)
            .andWhere { relatedTo.isNull() }
            .orderBy(SystemEventModel.createdAt, SortOrder.DESC)
            .map { it.toOutputDto() }
    }

    fun getWithRelated(eventId: Int): List<SystemEventOutputDto> = transaction {
        SystemEventModel
            .leftJoin(SystemEventTypeModel)
            .leftJoin(SystemEventObjectTypeModel)
//            .join(SystemEventModel, JoinType.LEFT, relatedTo, SystemEventModel.id)
            .sliceForOutput(needRollback = true)
            .select { SystemEventModel.id eq eventId }
            .map {
                it.toOutputDto(needRollback = true)
            }
    }

    fun getOne(eventId: Int): SystemEventOutputDto = transaction {
        SystemEventModel
            .leftJoin(SystemEventTypeModel)
            .leftJoin(SystemEventObjectTypeModel)
            .sliceForOutput(needRollback = true)
            .select { SystemEventModel.id eq eventId }
            .firstOrNull()
            ?.toOutputDto(needRollback = true)
            ?: throw NotFoundException("System event [$eventId] not found")
    }

    fun <T : SystemEventCreateDto> logEvent(event: T) = transaction {
        SystemEventModel.insert {
            it[author] = event.author
            it[eventObjectName] = event.eventObjectName
            it[eventObjectType] = event.eventObjectType
            it[eventDescription] = event.eventDescription
            it[eventType] = event.eventType
            it[canBeReset] = false
        }
    }

    fun <T : ResettableSystemEventCreateDto> logResettableEvent(event: T) {
        SystemEventModel.insert {
            it[author] = event.author
            it[eventObjectName] = event.eventObjectName
            it[eventObjectType] = event.eventObjectType
            it[eventDescription] = event.eventDescription
            it[eventType] = event.eventType
            it[eventObjectId] = event.eventObjectId
            it[rollbackRoute] = event.rollbackRoute
            it[rollbackInstance] = event.rollbackInstance
            it[canBeReset] = true
        }
    }
}