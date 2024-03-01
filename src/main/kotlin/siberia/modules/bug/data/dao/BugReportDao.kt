package siberia.modules.bug.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.bug.data.dto.BugReportOutputDto
import siberia.modules.bug.data.models.BugReportModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class BugReportDao (id: EntityID<Int>): BaseIntEntity<BugReportOutputDto>(id, BugReportModel) {

    companion object: BaseIntEntityClass<BugReportOutputDto, BugReportDao>(BugReportModel)

    var description by BugReportModel.description
    var user by BugReportModel.user
    override fun toOutputDto(): BugReportOutputDto =
        BugReportOutputDto(
            idValue,
            user,
            description
        )
}