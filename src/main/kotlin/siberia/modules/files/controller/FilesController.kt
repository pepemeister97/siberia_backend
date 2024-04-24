package siberia.modules.files.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.files.service.FilesService
import siberia.utils.files.FilesUtil
import siberia.utils.kodein.KodeinController

class FilesController(override val di: DI) : KodeinController() {
    private val fileService : FilesService by instance()
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
            route("original"){
                get("{filename}"){
                    val filename = call.parameters["filename"].toString()
                    val original = fileService.getOriginal(filename)
                    if (original != null){
                        val bytes = FilesUtil.read(original)
                        if (bytes != null)
                            call.respondBytes(bytes)
                        else
                            call.respond(HttpStatusCode.NotFound)
                    }else{
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}