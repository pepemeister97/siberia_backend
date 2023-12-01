package siberia.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.logging.*

object Logger {
    private val logger = mutableMapOf(
        "main" to KtorSimpleLogger("siberia.ExceptionFilter"),
        "database" to KtorSimpleLogger("siberia.ExceptionFilter.Database"),
        "Transformation" to KtorSimpleLogger("siberia.ExceptionFilter.Transformation")
    )

    fun callFailed(call: ApplicationCall, cause: Throwable, prefix: String = "main") {
        (logger[prefix].apply { if (this == null) logger["main"]!!.debug("Logger $prefix not found") } ?: logger["main"])!!.info("Request ${call.request.path()} was failed due to $cause")
        (logger[prefix] ?: logger["main"])!!.debug("Stacktrace => ${cause.stackTraceToString()}")
    }

    fun debug(message: Any?, prefix: String) {
        (logger[prefix].apply {
            if (this == null) logger["main"]!!.debug("Logger $prefix not found")
        } ?: logger["main"])!!.debug(message.toString())
    }
}
