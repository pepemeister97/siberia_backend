package siberia.modules.collection.service

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.collection.data.dto.CollectionInputDto
import siberia.modules.collection.data.dto.CollectionUpdateDto
import siberia.modules.collection.data.models.CollectionModel
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.logger.data.models.SystemEventModel
import siberia.utils.kodein.KodeinEventService

class CollectionEventService(di: DI) : KodeinEventService(di) {
    private val collectionService: CollectionService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) : Unit = transaction {
        val updateEventData = event.getRollbackData<CollectionUpdateDto>()
        with(
            CollectionModel.slice(CollectionModel.id).select {
                CollectionModel.id eq updateEventData.objectId
            }.map {
                it[CollectionModel.id]
            }
        ){
            if (this.isNotEmpty())
                collectionService.update(authorizedUser, updateEventData.objectId, updateEventData.objectDto)
            else throw BadRequestException("rollback failed model removed")
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val createEventData = event.getRollbackData<CollectionInputDto>()
        val createdCollection = collectionService.create(authorizedUser, createEventData.objectDto, shadowed = true)

        SystemEventModel.replaceRemovedWithNewId(
            event.eventObjectTypeId,
            createEventData.objectId,
            createdCollection.id
        )

        commit()
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}