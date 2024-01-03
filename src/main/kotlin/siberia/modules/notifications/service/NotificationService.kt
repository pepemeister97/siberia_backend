package siberia.modules.notifications.service

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import org.kodein.di.DI
import siberia.conf.AppConf
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.category.data.dao.CategoryDao.Companion.createBooleanCond
import siberia.modules.category.data.dao.CategoryDao.Companion.createLikeCond
import siberia.modules.category.data.dao.CategoryDao.Companion.createListCond
import siberia.modules.notifications.data.dao.NotificationDao
import siberia.modules.notifications.data.dao.NotificationDomainDao
import siberia.modules.notifications.data.dao.NotificationTypeDao
import siberia.modules.notifications.data.dto.NotificationCreateDto
import siberia.modules.notifications.data.dto.NotificationOutputDto
import siberia.modules.notifications.data.dto.NotificationSuccessWatchedDto
import siberia.modules.notifications.data.dto.NotificationsFilterDto
import siberia.modules.notifications.data.models.NotificationModel
import siberia.modules.transaction.data.dao.TransactionStatusDao
import siberia.modules.transaction.data.models.TransactionRelatedUserModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.transaction
import siberia.utils.kodein.KodeinService
import siberia.utils.websockets.dto.WebSocketResponseDto

class NotificationService(di: DI) : KodeinService(di) {
    private val connections: MutableMap<Int, MutableList<DefaultWebSocketSession>> = mutableMapOf()

    open class NotificationChannelEvent {
        companion object {
            private val serializer = Json { ignoreUnknownKeys = true }
        }
        class NewConnection(
            val authorizedUser: AuthorizedUser,
            val socketSession: DefaultWebSocketSession,
        ): NotificationChannelEvent()

        class EmitNotification(
            val notificationDao: NotificationDao
        ): NotificationChannelEvent() {

            fun getNotificationFrame(): Frame = Frame.Text(
                serializer.encodeToString(NotificationOutputDto.serializer(), notificationDao.toOutputDto())
            )
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private val notificationChannel = CoroutineScope(Job()).actor<NotificationChannelEvent>(capacity = Channel.BUFFERED) {
        for (event in this) {
            when (event) {
                is NotificationChannelEvent.NewConnection -> {
                    val connectionsByUser = connections[event.authorizedUser.id]
                    if (connectionsByUser != null)
                        connectionsByUser.add(event.socketSession)
                    else
                        connections[event.authorizedUser.id] = mutableListOf(event.socketSession)

                    //After every new connection check for inactive ones
                    connections[event.authorizedUser.id] =
                        connections[event.authorizedUser.id]?.filter {
                            it.isActive
                        }?.toMutableList() ?: mutableListOf()
                }

                is NotificationChannelEvent.EmitNotification -> {
                    val connectionsByUser = connections[event.notificationDao.targetId] ?: continue
                    connectionsByUser.forEach {
                        if (it.isActive)
                            it.send(WebSocketResponseDto.wrap("new-notification", event.getNotificationFrame()).json)
                    }
                }
            }
        }
    }
    fun getNotifications(userId: Int, notificationsFilter: NotificationsFilterDto): List<NotificationOutputDto> = transaction {
        NotificationDao.find {
            NotificationModel.target eq userId and
            createListCond(notificationsFilter.domain, NotificationModel.id neq 0, NotificationModel.domain) and
            createListCond(notificationsFilter.type, NotificationModel.id neq 0, NotificationModel.type) and
            createLikeCond(notificationsFilter.description, NotificationModel.id neq 0, NotificationModel.description) and
            createBooleanCond(notificationsFilter.new, NotificationModel.id neq 0, NotificationModel.watched, reversed = true)
        }.map { it.toOutputDto() }
    }

    fun setWatched(userId: Int, onWatch: List<Int>): NotificationSuccessWatchedDto = transaction {
        NotificationModel.update({ (NotificationModel.target eq userId) and (NotificationModel.id inList onWatch) } ) {
            it[watched] = true
        }
        commit()

        NotificationSuccessWatchedDto(true)
    }

    private fun createNotification(notificationCreateDto: NotificationCreateDto) = transaction {
        val createdNotificationDao = NotificationDao.new {
            target = UserDao[notificationCreateDto.targetId]
            type = NotificationTypeDao[notificationCreateDto.typeId]
            domain = NotificationDomainDao[notificationCreateDto.domainId]
            description = notificationCreateDto.description
        }

        notificationChannel.trySend(NotificationChannelEvent.EmitNotification(
            createdNotificationDao
        ))
    }

    fun newConnection(authorizedUser: AuthorizedUser, socketSession: DefaultWebSocketSession) {
        notificationChannel.trySend(NotificationChannelEvent.NewConnection(
            authorizedUser, socketSession
        ))
    }

    fun notifyTransactionStatusChange(transactionId: Int, changedTo: Int) {
        val statusAfterDto = TransactionStatusDao[changedTo].toOutputDto()
        TransactionRelatedUserModel.getRelatedUsers(transactionId).forEach {
             createNotification(NotificationCreateDto(
                targetId = it,
                typeId = AppConf.notificationTypes.info,
                domainId = AppConf.notificationDomains.transactions,
                description = "Status of transaction (id = $transactionId) was changed to ${statusAfterDto.name}"
            ))
        }
    }
}