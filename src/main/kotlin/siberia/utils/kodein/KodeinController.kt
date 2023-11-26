package siberia.utils.kodein

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.kodein.di.DIAware
import org.kodein.di.instance
import siberia.modules.auth.data.dto.authorization.RefreshTokenDto
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.user.data.dto.AuthorizedUser

/**
 * A [KodeinAware] base class for Controllers handling routes.
 * It allows to easily get dependencies, and offers some useful extensions.
 */
@Suppress("KDocUnresolvedReference")
abstract class KodeinController : DIAware {
    /**
     * Injected dependency with the current [Application].
     */
    val application: Application by instance()

    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    abstract fun Routing.registerRoutes()

    fun getAuthorized(call: ApplicationCall): AuthorizedUser {
        val principal = call.principal<JWTPrincipal>()!!
        Json.decodeFromString<List<LinkedRuleOutputDto>>(principal.getClaim("rules", String::class) ?: "[]")
        return AuthorizedUser(
            id = principal.getClaim("id", Int::class)!!,
            rules = Json.decodeFromString<List<LinkedRuleOutputDto>>(principal.getClaim("rules", String::class) ?: "[]")
        )
    }

    fun getRefresh(call: ApplicationCall): RefreshTokenDto {
        val principal = call.principal<JWTPrincipal>()!!
        return RefreshTokenDto(
            id = principal.getClaim("id", Int::class)!!,
            lastLogin = principal.getClaim("lastLogin", Long::class)!!
        )
    }
}