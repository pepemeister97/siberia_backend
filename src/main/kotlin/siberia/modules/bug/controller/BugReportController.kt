package siberia.modules.bug.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.bug.data.dto.BugReportCreateDto
import siberia.modules.bug.data.dto.BugReportSearchFilterDto
import siberia.modules.bug.service.BugReportService
import siberia.utils.kodein.KodeinController

class BugReportController(override val di: DI) : KodeinController() {
    private val bugReportService: BugReportService by instance()
    override fun Routing.registerRoutes() {
        route("bug") {
            authenticate("mobile-access") {
                post("report") {
                    val report = call.receive<BugReportCreateDto>()
                    val authorizedUser = call.getAuthorized()
                    call.respond(bugReportService.create(authorizedUser, report))
                }
                post("filter") {
                    val filter = call.receive<BugReportSearchFilterDto>()
                    call.respond(bugReportService.getByFilter(filter))
                }
            }
        }
    }
}