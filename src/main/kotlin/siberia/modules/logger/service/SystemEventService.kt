package siberia.modules.logger.service

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.kodein.di.DI
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.logger.data.dto.SystemEventSearchFilter
import siberia.modules.logger.data.models.SystemEventModel
import siberia.utils.kodein.KodeinService
import java.time.LocalDateTime

class SystemEventService(di: DI) : KodeinService(di) {
    private fun userCond(userName: String?): Op<Boolean> =
        if (userName == null)
            SystemEventModel.author.isNotNull()
        else
            SystemEventModel.author eq userName

    private fun timeCond(range: Pair<LocalDateTime?, LocalDateTime?>?): Op<Boolean> =
        if (range == null)
            SystemEventModel.createdAt.isNotNull()
        else {
            val leftBound = (range.first ?: LocalDateTime.MIN)
            val rightBound = (range.second ?: LocalDateTime.MAX)
            (SystemEventModel.createdAt lessEq rightBound) and
            (SystemEventModel.createdAt greaterEq leftBound)
        }


    private fun typeCond(eventType: Int?) =
        if (eventType == null)
            SystemEventModel.id.isNotNull()
        else
            SystemEventModel.eventType eq eventType

    fun getByFilter(systemEventSearchFilter: SystemEventSearchFilter?): List<SystemEventOutputDto> =
        if (systemEventSearchFilter == null) {
            SystemEventModel.getList { SystemEventModel.id.isNotNull() }
        } else
            SystemEventModel.getList {
                userCond(systemEventSearchFilter.userName) and
                typeCond(systemEventSearchFilter.type) and
                timeCond(systemEventSearchFilter.range)
            }
}