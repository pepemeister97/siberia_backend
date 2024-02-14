package siberia.modules.category.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.category.data.dto.CategoryInputDto
import siberia.modules.category.data.dto.CategoryOnRemoveDto
import siberia.modules.category.data.dto.CategoryUpdateDto
import siberia.modules.category.service.CategoryCache
import siberia.modules.category.service.CategoryEventService
import siberia.modules.category.service.CategoryService
import siberia.utils.kodein.KodeinController

class CategoryController(override val di: DI) : KodeinController() {
    private val categoryService: CategoryService by instance()
    private val categoryEventService: CategoryEventService by instance()
    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    override fun Routing.registerRoutes() {
        route("category") {
            authenticate ("default") {
                get {
                    call.respond(CategoryCache.tryGetFullTree { categoryService.getAll() })
                }
                get("{categoryId}") {
                    val categoryId: Int = call.parameters.getInt("categoryId", "Category id must be INT")

                    call.respond(categoryService.getOne(categoryId))
                }
            }
            authenticate ("category-managing") {
                post {
                    val categoryInputDto = call.receive<CategoryInputDto>()
                    val authorizedUser = call.getAuthorized()

                    CategoryCache.invalidate()

                    call.respond(categoryService.create(authorizedUser, categoryInputDto))
                }
                post ("rollback/{eventId}") {
                    val authorizedUser = call.getAuthorized()
                    val eventId = call.parameters.getInt("eventId", "Event id must be INT")

                    CategoryCache.invalidate()

                    call.respond(categoryEventService.rollback(authorizedUser, eventId))
                }
                route("{categoryId}") {
                    patch {
                        val categoryId: Int = call.parameters.getInt("categoryId", "Category id must be INT")
                        val categoryUpdateDto = call.receive<CategoryUpdateDto>()
                        val authorizedUser = call.getAuthorized()

                        CategoryCache.invalidate()

                        call.respond(categoryService.update(authorizedUser, categoryId, categoryUpdateDto))
                    }
                    delete {
                        val categoryId: Int = call.parameters.getInt("categoryId", "Category id must be INT")
                        val categoryOnRemoveDto = call.receive<CategoryOnRemoveDto>()
                        val authorizedUser = call.getAuthorized()

                        CategoryCache.invalidate()

                        call.respond(categoryService.remove(authorizedUser, categoryId, categoryOnRemoveDto))
                    }
                }
            }
        }
    }
}