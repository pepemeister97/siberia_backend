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
        fun new(authorName: String, shadowed: Boolean = false, init: CollectionDao.() -> Unit): CollectionDao {
            val collectionDao = super.new(init)
            val event = CollectionCreateEvent(authorName, collectionDao.name, collectionDao.idValue)
            if (!shadowed)
                SystemEventModel.logEvent(event)
            return collectionDao
        }

    }

    var name by CollectionModel.name

    override fun toOutputDto(): CollectionOutputDto =
        CollectionOutputDto(idValue, name)

    fun loadAndFlush(authorName: String, collectionUpdateDto: CollectionUpdateDto, batch: EntityBatchUpdate? = null): Boolean {
        val event = CollectionUpdateEvent(
            authorName,
            with(collectionUpdateDto) {
                if (name == this@CollectionDao.name || name == null) this@CollectionDao.name
                else "$name (${this@CollectionDao.name})"
            },
            createEncodedRollbackUpdateDto<CollectionOutputDto, CollectionUpdateDto>(collectionUpdateDto),
            idValue
        )
        SystemEventModel.logResettableEvent(event)
        name = collectionUpdateDto.name ?: name
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