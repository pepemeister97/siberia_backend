package siberia.modules.product.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.modules.product.data.dto.ProductMassiveUpdateDto
import siberia.modules.product.data.dto.groups.*
import siberia.modules.product.service.ProductGroupService
import siberia.utils.kodein.KodeinController

class ProductGroupController(override val di: DI) : KodeinController() {
    private val productGroupService: ProductGroupService by instance()
    override fun Routing.registerRoutes() {
        authenticate("products-managing") {
            route("product/groups") {
                get {
                    call.respond<List<ProductGroupOutputDto>>(productGroupService.getAll())
                }
                post {
                    val productGroupCreateDto = call.receive<ProductGroupCreateDto>()
                    val authorizedUser = call.getAuthorized()

                    call.respond<ProductGroupFullOutputDto>(productGroupService.create(authorizedUser, productGroupCreateDto))
                }
                route ("{groupId}") {
                    get {
                        val groupId = call.parameters.getInt("groupId", "Group id must be INT")

                        call.respond<ProductGroupFullOutputDto>(productGroupService.getOne(groupId))
                    }
                    patch {
                        val groupId = call.parameters.getInt("groupId", "Group id must be INT")
                        val authorizedUser = call.getAuthorized()

                        val productGroupUpdateDto = call.receive<ProductGroupUpdateDto>()

                        call.respond<ProductGroupOutputDto>(productGroupService.update(authorizedUser, groupId, productGroupUpdateDto))
                    }
                    delete {
                        val groupId = call.parameters.getInt("groupId", "Group id must be INT")
                        val authorizedUser = call.getAuthorized()

                        call.respond<ProductGroupActionResultDto>(productGroupService.remove(authorizedUser, groupId))
                    }
                    post("apply") {
                        val authorizedUser = call.getAuthorized()
                        val groupId = call.parameters.getInt("groupId", "Group id must be INT")
                        val productUpdateDto = call.receive<ProductMassiveUpdateDto>()

                        call.respond<ProductGroupActionResultDto>(productGroupService.updateGroup(authorizedUser, groupId, productUpdateDto))
                    }
                }
            }
        }
    }
}