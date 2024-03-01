package siberia.modules.bug.service

import org.jetbrains.exposed.sql.and
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.bug.data.dao.BugReportDao
import siberia.modules.bug.data.dto.BugReportCreateDto
import siberia.modules.bug.data.dto.BugReportOutputDto
import siberia.modules.bug.data.dto.BugReportSearchFilterDto
import siberia.modules.bug.data.models.BugReportModel
import siberia.modules.transaction.data.dao.TransactionStatusDao.Companion.createLikeCond
import siberia.modules.transaction.data.dao.TransactionStatusDao.Companion.timeCond
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.transaction
import siberia.utils.kodein.KodeinService

class BugReportService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, bug : BugReportCreateDto) : BugReportOutputDto = transaction{
        val userDao = UserDao[authorizedUser.id]
        val bugReportDao = BugReportDao.new {
            user = userDao.login
            description = bug.description
        }
        commit()
        bugReportDao.toOutputDto()
    }
    fun getByFilter(filter : BugReportSearchFilterDto?) : List<BugReportOutputDto> = transaction {
        if (filter == null) {
            BugReportDao.all()
        } else {
            BugReportDao.find {
                createLikeCond(filter.author, BugReportModel.id neq 0, BugReportModel.user) and
                        timeCond(Pair(filter.rangeStart, filter.rangeEnd), BugReportModel.createdAt)
            }
        }
    }.map { it.toOutputDto() }
}