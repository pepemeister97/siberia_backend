package siberia.modules.collection.data.dao

import org.jetbrains.exposed.dao.EntityBatchUpdate
import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.collection.data.dto.CollectionOutputDto
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

    fun flush(authorName: String, batch: EntityBatchUpdate? = null): Boolean {
        val event = CollectionUpdateEvent(authorName, name)
        SystemEventModel.logEvent(event)
        return super.flush(batch)
    }

    fun delete(authorName: String) {
        val event = CollectionRemoveEvent(authorName, name)
        SystemEventModel.logEvent(event)
        super.delete()
    }
}