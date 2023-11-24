package siberia

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import siberia.conf.AppConf
import siberia.plugins.*

fun main() {
    embeddedServer(Netty, port = AppConf.server.port, host = AppConf.server.host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureCORS()
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureExceptionFilter()
}
