package siberia.modules.logger.service

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.kodein.di.DI
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.logger.data.dto.SystemEventSearchFilter
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.rbac.data.dao.RuleDao.Companion.createLikeCond
import siberia.modules.rbac.data.dao.RuleDao.Companion.createListCond
import siberia.utils.kodein.KodeinService
import java.time.LocalDateTime
import java.time.ZoneOffset

class SystemEventService(di: DI) : KodeinService(di) {

    private fun timeCond(range: Pair<Long?, Long?>?): Op<Boolean> {
        return if (range == null)
            SystemEventModel.createdAt.isNotNull()
        else {
            val leftBound = range.first.let {
                if (it == null)
                    LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(3))
                else {
                    val seconds: Long = it / 1000
                    val nanos: Int = (it % 1000).toInt()
                    LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.ofHours(3))
                }
            }
            val rightBound = range.second.let {
                if (it == null) //Use INT.MAX * 2 (2106 year) because Long.MAX_VALUE is too big for timestamp
                    LocalDateTime.ofEpochSecond(Int.MAX_VALUE.toLong() * 2, 0, ZoneOffset.ofHours(3))
                else {
                    val seconds: Long = it / 1000
                    val nanos: Int = (it % 1000).toInt()
                    LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.ofHours(3))
                }
            }
            (SystemEventModel.createdAt lessEq rightBound) and
            (SystemEventModel.createdAt greaterEq leftBound)
        }
    }

    fun getByFilter(systemEventSearchFilter: SystemEventSearchFilter?): List<SystemEventOutputDto> =
        if (systemEventSearchFilter == null) {
            SystemEventModel.getList { SystemEventModel.id.isNotNull() }
        } else {
            SystemEventModel.getList {
                createLikeCond(systemEventSearchFilter.author, SystemEventModel.id.isNotNull(), SystemEventModel.author) and
                createListCond(systemEventSearchFilter.eventTypeId, SystemEventModel.id.isNotNull(), SystemEventModel.eventType) and
                createListCond(systemEventSearchFilter.eventObjectTypeId, SystemEventModel.id.isNotNull(), SystemEventModel.eventObjectType) and
                timeCond(Pair(systemEventSearchFilter.rangeStart, systemEventSearchFilter.rangeEnd))
            }
        }.toList()
}