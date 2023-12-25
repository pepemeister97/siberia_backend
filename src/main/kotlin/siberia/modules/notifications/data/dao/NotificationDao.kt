package siberia.modules.notifications.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.notifications.data.dto.NotificationOutputDto
import siberia.modules.notifications.data.models.NotificationModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class NotificationDao(id: EntityID<Int>) : BaseIntEntity<NotificationOutputDto>(id, NotificationModel) {
    companion object : BaseIntEntityClass<NotificationOutputDto, NotificationDao>(NotificationModel)

    private val _targetId by NotificationModel.target
    val targetId: Int = _targetId.value
    var target by UserDao referencedOn NotificationModel.target

    val watched by NotificationModel.watched
    var description by NotificationModel.description

    private val _typeId by NotificationModel.type
    val typeId: Int = _typeId.value
    var type by NotificationTypeDao referencedOn NotificationModel.type

    private val _domainId by NotificationModel.domain
    val domainId: Int = _domainId.value
    var domain by NotificationDomainDao referencedOn NotificationModel.domain
    override fun toOutputDto(): NotificationOutputDto =
        NotificationOutputDto(
            idValue, watched, type.toOutputDto(), domain.toOutputDto(), description
        )
}