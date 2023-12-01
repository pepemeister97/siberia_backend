package siberia.plugins

import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.logging.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import siberia.conf.AppConf
import siberia.exceptions.BaseException
import siberia.exceptions.InternalServerException

fun Application.configureExceptionFilter() {
    val logger = mutableMapOf(
        "main" to KtorSimpleLogger("siberia.ExceptionFilter"),
        "database" to KtorSimpleLogger("siberia.ExceptionFilter.Database"),
        "Transformation" to KtorSimpleLogger("siberia.ExceptionFilter.Transformation")
    )
    install(StatusPages) {
        fun log(call: ApplicationCall, cause: Throwable, prefix: String = "main") {
            (logger[prefix] ?: logger["main"])!!.info("Request ${call.request.path()} was failed due to $cause")
            (logger[prefix] ?: logger["main"])!!.debug("Stacktrace => ${cause.stackTraceToString()}")
        }
        fun Throwable.getClientMessage(): String = if (AppConf.isDebug) message.toString() else ""

        exception<Throwable> {
            call, cause ->
                log(call, cause)
                call.respond<InternalServerException>(
                    HttpStatusCode.InternalServerError,
                    InternalServerException(cause.getClientMessage())
                )
        }

        exception<ExposedSQLException> {
            call, exposedSqlException ->
                log(call, exposedSqlException, "Database")
                call.respond<InternalServerException>(
                    HttpStatusCode.InternalServerError,
                    InternalServerException(exposedSqlException.getClientMessage())
                )
        }

        exception<NoTransformationFoundException> {
            call, requestValidationException ->
                log(call, requestValidationException)
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = InternalServerException(requestValidationException.getClientMessage())
                )
        }

        exception<BaseException> {
            call, cause ->
                log(call, cause)
                call.respond(
                    status = HttpStatusCode(cause.httpStatusCode, cause.httpStatusText),
                    cause
                )
        }
    }
}
