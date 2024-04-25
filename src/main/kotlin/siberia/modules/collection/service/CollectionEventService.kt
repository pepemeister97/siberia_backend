package siberia.modules.collection.service

import org.jetbrains.exposed.sql.select
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.collection.data.dto.CollectionInputDto
import siberia.modules.collection.data.dto.CollectionUpdateDto
import siberia.modules.collection.data.models.CollectionModel
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.utils.kodein.KodeinEventService

class CollectionEventService(di: DI) : KodeinEventService(di) {
    private val collectionService: CollectionService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val updateEventData = event.getRollbackData<CollectionUpdateDto>()
        with(
            CollectionModel.select {
                CollectionModel.id eq updateEventData.objectId
            }.map {
                it[CollectionModel.id]
            }
        ){
            if (this.isNotEmpty())
                collectionService.update(authorizedUser, updateEventData.objectId, updateEventData.objectDto)
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        val createEventData = event.getRollbackData<CollectionInputDto>()
        collectionService.create(authorizedUser, createEventData.objectDto)
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}