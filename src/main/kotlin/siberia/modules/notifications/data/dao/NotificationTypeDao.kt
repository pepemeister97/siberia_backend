package siberia.modules.notifications.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.notifications.data.dto.NotificationTypeOutputDto
import siberia.modules.notifications.data.models.NotificationTypeModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class NotificationTypeDao(id: EntityID<Int>) : BaseIntEntity<NotificationTypeOutputDto>(id, NotificationTypeModel) {
    companion object : BaseIntEntityClass<NotificationTypeOutputDto, NotificationTypeDao>(NotificationTypeModel)

    val name by NotificationTypeModel.name
    override fun toOutputDto(): NotificationTypeOutputDto =
        NotificationTypeOutputDto(idValue, name)
}