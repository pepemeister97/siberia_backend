package siberia.modules.brand.data.dao

import org.jetbrains.exposed.dao.EntityBatchUpdate
import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.brand.data.dto.BrandOutputDto
import siberia.modules.brand.data.dto.systemevents.BrandCreateEvent
import siberia.modules.brand.data.dto.systemevents.BrandRemoveEvent
import siberia.modules.brand.data.dto.systemevents.BrandUpdateEvent
import siberia.modules.brand.data.models.BrandModel
import siberia.modules.logger.data.models.SystemEventModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class BrandDao(id: EntityID<Int>) : BaseIntEntity<BrandOutputDto>(id, BrandModel) {

    companion object : BaseIntEntityClass<BrandOutputDto, BrandDao>(BrandModel) {
        fun new(authorName: String, init: BrandDao.() -> Unit): BrandDao {
            val brandDao = super.new(init)
            val event = BrandCreateEvent(authorName, brandDao.name)
            SystemEventModel.logEvent(event)
            return brandDao
        }

    }

    var name by BrandModel.name

    override fun toOutputDto(): BrandOutputDto =
        BrandOutputDto(idValue, name)

    fun flush(authorName: String, batch: EntityBatchUpdate? = null): Boolean {
        val event = BrandUpdateEvent(authorName, name)
        SystemEventModel.logEvent(event)
        return super.flush(batch)
    }

    fun delete(authorName: String) {
        val event = BrandRemoveEvent(authorName, name)
        SystemEventModel.logEvent(event)
        super.delete()
    }
}