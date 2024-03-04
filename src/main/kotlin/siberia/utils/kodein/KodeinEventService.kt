package siberia.utils.kodein

import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.conf.AppConf
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.logger.data.models.SystemEventModel

abstract class KodeinEventService(di: DI) : KodeinService(di) {
    protected abstract fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto)
    protected abstract fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto)
    protected abstract fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto)

    fun rollback(authorizedUser: AuthorizedUser, eventId: Int) {
        val events = SystemEventModel.getWithRelated(eventId)
        val rollbackEvents = events.mapNotNull { event ->
            if (!event.canBeReset)
                return@mapNotNull null
            when (event.eventTypeId) {
                AppConf.eventTypes.updateEvent -> rollbackUpdate(authorizedUser, event)
                AppConf.eventTypes.removeEvent -> rollbackRemove(authorizedUser, event)
                AppConf.eventTypes.createEvent -> rollbackCreate(authorizedUser, event)
            }
            event.id
        }
        transaction {
            SystemEventModel.deleteWhere { id inList rollbackEvents }
        }
    }
}