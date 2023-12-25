package siberia.modules.notifications.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.notifications.data.dto.NotificationDomainOutputDto
import siberia.modules.notifications.data.models.NotificationDomainModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class NotificationDomainDao(id: EntityID<Int>) : BaseIntEntity<NotificationDomainOutputDto>(id, NotificationDomainModel) {
    companion object : BaseIntEntityClass<NotificationDomainOutputDto, NotificationDomainDao>(NotificationDomainModel)

    val name by NotificationDomainModel.name
    override fun toOutputDto(): NotificationDomainOutputDto =
        NotificationDomainOutputDto(idValue, name)
}