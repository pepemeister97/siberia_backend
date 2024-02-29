package siberia.modules.collection.data.dao

import org.jetbrains.exposed.dao.EntityBatchUpdate
import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.collection.data.dto.CollectionOutputDto
import siberia.modules.collection.data.dto.CollectionUpdateDto
import siberia.modules.collection.data.dto.systemevents.CollectionCreateEvent
import siberia.modules.collection.data.dto.systemevents.CollectionRemoveEvent
import siberia.modules.collection.data.dto.systemevents.CollectionUpdateEvent
import siberia.modules.collection.data.models.CollectionModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class CollectionDao(id: EntityID<Int>) : BaseIntEntity<CollectionOutputDto>(id, CollectionModel) {

    companion object : BaseIntEntityClass<CollectionOutputDto, CollectionDao>(CollectionModel) {
        fun new(authorName: String, init: CollectionDao.() -> Unit): CollectionDao {
            val brandDao = super.new(init)
            val event = CollectionCreateEvent(authorName, brandDao.name)
            SystemEventModel.logEvent(event)
            return brandDao
        }

    }

    var name by CollectionModel.name

    override fun toOutputDto(): CollectionOutputDto =
        CollectionOutputDto(idValue, name)

    fun loadAndFlush(authorName: String, collectionUpdateDto: CollectionUpdateDto, batch: EntityBatchUpdate? = null): Boolean {
        val nameOnUpdate = collectionUpdateDto.name ?: return true
        val event = CollectionUpdateEvent(
            authorName,
            name,
            createEncodedRollbackUpdateDto<CollectionOutputDto, CollectionUpdateDto>(collectionUpdateDto),
            idValue
        )
        SystemEventModel.logResettableEvent(event)
        name = nameOnUpdate
        return super.flush(batch)
    }

    fun delete(authorName: String) {
        val event = CollectionRemoveEvent(
            authorName,
            name,
            createRollbackRemoveDto<CollectionOutputDto>(),
            idValue
        )
        SystemEventModel.logResettableEvent(event)
        super.delete()
    }
}