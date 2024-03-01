package siberia.modules.logger.service

import org.jetbrains.exposed.sql.and
import org.kodein.di.DI
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.logger.data.dto.SystemEventSearchFilter
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.logger.data.models.SystemEventObjectTypeModel
import siberia.modules.logger.data.models.SystemEventTypeModel
import siberia.modules.rbac.data.dao.RuleDao.Companion.createLikeCond
import siberia.modules.rbac.data.dao.RuleDao.Companion.createListCond
import siberia.modules.rbac.data.dao.RuleDao.Companion.timeCond
import siberia.utils.kodein.KodeinService

class SystemEventService(di: DI) : KodeinService(di) {

    fun getByFilter(systemEventSearchFilter: SystemEventSearchFilter?): List<SystemEventOutputDto> =
        if (systemEventSearchFilter == null) {
            SystemEventModel.getList { SystemEventModel.id.isNotNull() }
        } else {
            SystemEventModel.getList {
                createLikeCond(systemEventSearchFilter.author, SystemEventModel.id.isNotNull(), SystemEventModel.author) and
                createListCond(systemEventSearchFilter.eventTypeId, SystemEventModel.id.isNotNull(), SystemEventModel.eventType) and
                createListCond(systemEventSearchFilter.eventObjectTypeId, SystemEventModel.id.isNotNull(), SystemEventModel.eventObjectType) and
                timeCond(Pair(systemEventSearchFilter.rangeStart, systemEventSearchFilter.rangeEnd), SystemEventModel.createdAt)
            }
        }.toList()

    fun getAllTypes() = SystemEventTypeModel.getAll()

    fun getAllObjectTypes() = SystemEventObjectTypeModel.getAll()
}