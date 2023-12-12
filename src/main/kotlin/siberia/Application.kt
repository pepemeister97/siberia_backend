package siberia

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import siberia.conf.AppConf
import siberia.modules.auth.controller.AuthController
import siberia.modules.auth.data.models.role.RoleModel
import siberia.modules.auth.data.models.rule.RuleCategoryModel
import siberia.modules.auth.data.models.rule.RuleModel
import siberia.modules.auth.service.AuthService
import siberia.modules.product.data.models.ProductCategoryModel
import siberia.modules.product.data.models.ProductCategoryToProductCategoryModel
import siberia.modules.product.data.models.ProductCollectionModel
import siberia.modules.product.data.models.ProductModel
import siberia.modules.stock.data.models.StockModel
import siberia.modules.stock.data.models.StockProductsModel
import siberia.modules.user.controller.RbacController
import siberia.modules.user.controller.UserController
import siberia.modules.user.data.models.UserModel
import siberia.modules.user.service.RbacService
import siberia.modules.user.service.UserAccessControlService
import siberia.plugins.*
import siberia.utils.database.DatabaseConnector
import siberia.utils.kodein.bindSingleton
import siberia.utils.kodein.kodeinApplication

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

    kodeinApplication {
        bindSingleton { AuthService(it) }
        bindSingleton { UserAccessControlService(it) }
        bindSingleton { RbacService(it) }

        bindSingleton { AuthController(it) }
        bindSingleton { UserController(it) }
        bindSingleton { RbacController(it) }
    }

    DatabaseConnector(
        UserModel,
        RoleModel, RuleModel, RuleCategoryModel,
        StockModel, StockProductsModel,
        ProductModel, ProductCategoryModel, ProductCollectionModel, ProductCategoryToProductCategoryModel
    ) {

    }
}
