package siberia

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import siberia.conf.AppConf
import siberia.conf.DatabaseInitializer
import siberia.modules.auth.controller.AuthController
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.rbac.data.models.rule.RuleCategoryModel
import siberia.modules.rbac.data.models.rule.RuleModel
import siberia.modules.auth.service.AuthService
import siberia.modules.brand.controller.BrandController
import siberia.modules.brand.data.models.BrandModel
import siberia.modules.brand.service.BrandService
import siberia.modules.category.controller.CategoryController
import siberia.modules.category.data.models.CategoryModel
import siberia.modules.category.data.models.CategoryToCategoryModel
import siberia.modules.category.service.CategoryService
import siberia.modules.collection.controller.CollectionController
import siberia.modules.collection.data.models.CollectionModel
import siberia.modules.collection.service.CollectionService
import siberia.modules.logger.controller.SystemEventController
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.logger.data.models.SystemEventObjectTypeModel
import siberia.modules.logger.data.models.SystemEventTypeModel
import siberia.modules.logger.service.SystemEventService
import siberia.modules.notifications.controller.NotificationsWebSocketController
import siberia.modules.notifications.data.models.NotificationModel
import siberia.modules.notifications.data.models.NotificationTypeModel
import siberia.modules.notifications.service.NotificationService
import siberia.modules.product.controller.ProductController
import siberia.modules.product.data.models.ProductModel
import siberia.modules.product.service.ProductService
import siberia.modules.stock.data.models.StockModel
import siberia.modules.stock.data.models.StockToProductModel
import siberia.modules.rbac.controller.RbacController
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.user.controller.UserController
import siberia.modules.user.data.models.UserModel
import siberia.modules.rbac.service.RbacService
import siberia.modules.stock.controller.StockController
import siberia.modules.stock.service.StockService
import siberia.modules.transaction.controller.TransactionController
import siberia.modules.transaction.data.models.*
import siberia.modules.transaction.service.TransactionService
import siberia.modules.user.service.UserAccessControlService
import siberia.modules.user.service.UserService
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
        bindSingleton { UserService(it) }
        bindSingleton { UserAccessControlService(it) }
        bindSingleton { RbacService(it) }
        bindSingleton { SystemEventService(it) }
        bindSingleton { BrandService(it) }
        bindSingleton { CollectionService(it) }
        bindSingleton { CategoryService(it) }
        bindSingleton { ProductService(it) }
        bindSingleton { StockService(it) }
        bindSingleton { TransactionService(it) }
        bindSingleton { NotificationService(it) }

        bindSingleton { AuthController(it) }
        bindSingleton { UserController(it) }
        bindSingleton { RbacController(it) }
        bindSingleton { CollectionController(it) }
        bindSingleton { BrandController(it) }
        bindSingleton { CategoryController(it) }
        bindSingleton { SystemEventController(it) }
        bindSingleton { ProductController(it) }
        bindSingleton { StockController(it) }
        bindSingleton { TransactionController(it) }
        bindSingleton { NotificationsWebSocketController(it) }
    }

    DatabaseConnector(
        UserModel,
        RbacModel, RoleModel, RuleModel, RuleCategoryModel,
        StockModel, StockToProductModel,
        BrandModel, CollectionModel,
        CategoryModel, CategoryToCategoryModel,
        ProductModel,
        SystemEventModel, SystemEventTypeModel, SystemEventObjectTypeModel,
        TransactionModel, TransactionToProductModel, TransactionRelatedUserModel, TransactionStatusModel, TransactionTypeModel,
        NotificationModel, NotificationTypeModel, NotificationTypeModel
    ) {
        DatabaseInitializer.initRules()
        DatabaseInitializer.initEventTypes()
        DatabaseInitializer.initObjectTypes()
        DatabaseInitializer.initRequestTypes()
        DatabaseInitializer.initRequestStatuses()
        DatabaseInitializer.initNotificationTypes()
        DatabaseInitializer.initNotificationDomains()
        DatabaseInitializer.initUsers()
        DatabaseInitializer.initCategory()
        commit()
    }
}
