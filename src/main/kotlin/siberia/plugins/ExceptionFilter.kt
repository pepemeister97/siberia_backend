package siberia.plugins

import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.exceptions.BaseException
import siberia.exceptions.InternalServerException
import siberia.exceptions.NotFoundException
import io.ktor.server.plugins.BadRequestException as BadRequestExceptionKtor

fun Application.configureExceptionFilter() {

    install(StatusPages) {
        fun Throwable.getClientMessage(): String = if (AppConf.isDebug) message.toString() else ""

        exception<Throwable> {
            call, cause ->
                Logger.callFailed(call, cause)
                call.respond<InternalServerException>(
                    HttpStatusCode.InternalServerError,
                    InternalServerException(cause.getClientMessage())
                )
        }

        exception<ExposedSQLException> {
            call, exposedSqlException ->
                Logger.callFailed(call, exposedSqlException, "Database")
                call.respond<InternalServerException>(
                    HttpStatusCode.InternalServerError,
                    InternalServerException(exposedSqlException.getClientMessage())
                )
        }

        exception<EntityNotFoundException> {
            call, exposedException ->
                Logger.callFailed(call, exposedException, "Database")
                call.respond<NotFoundException>(
                    HttpStatusCode.NotFound,
                    NotFoundException(exposedException.getClientMessage())
                )
        }

        exception<NoTransformationFoundException> {
            call, requestValidationException ->
                Logger.callFailed(call, requestValidationException)
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = InternalServerException(requestValidationException.getClientMessage())
                )
        }

        exception<BadRequestExceptionKtor> {
            call, requestValidationException ->
                Logger.callFailed(call, requestValidationException)
                call.respond(
                    status = HttpStatusCode.UnsupportedMediaType,
                    message = BadRequestException(requestValidationException.getClientMessage())
                )
        }

        exception<BaseException> {
            call, cause ->
                Logger.callFailed(call, cause)
                call.respond(
                    status = HttpStatusCode(cause.httpStatusCode, cause.httpStatusText),
                    cause
                )
        }
    }
}
