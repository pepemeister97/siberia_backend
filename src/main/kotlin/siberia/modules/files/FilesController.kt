package siberia.modules.files

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import siberia.utils.files.FilesUtil
import siberia.utils.kodein.KodeinController

class FilesController(override val di: DI) : KodeinController() {
    override fun Routing.registerRoutes() {
        route ("file/stream") {
            get ("{filename}") {
                val filename = call.parameters["filename"].toString()
                val bytes = FilesUtil.read(filename)
                if (bytes != null)
                    call.respondBytes(bytes)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}