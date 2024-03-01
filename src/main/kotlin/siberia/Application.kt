package siberia

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import siberia.conf.AppConf
import siberia.conf.DatabaseInitializer
import siberia.modules.auth.controller.AuthController
import siberia.modules.auth.service.AuthQrService
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.rbac.data.models.rule.RuleCategoryModel
import siberia.modules.rbac.data.models.rule.RuleModel
import siberia.modules.auth.service.AuthService
import siberia.modules.auth.service.AuthSocketService
import siberia.modules.brand.controller.BrandController
import siberia.modules.brand.data.models.BrandModel
import siberia.modules.brand.service.BrandEventService
import siberia.modules.brand.service.BrandService
import siberia.modules.bug.controller.BugReportController
import siberia.modules.bug.data.models.BugReportModel
import siberia.modules.bug.service.BugReportService
import siberia.modules.category.controller.CategoryController
import siberia.modules.category.data.models.CategoryModel
import siberia.modules.category.data.models.CategoryToCategoryModel
import siberia.modules.category.service.CategoryEventService
import siberia.modules.category.service.CategoryService
import siberia.modules.collection.controller.CollectionController
import siberia.modules.collection.data.models.CollectionModel
import siberia.modules.collection.service.CollectionEventService
import siberia.modules.collection.service.CollectionService
import siberia.modules.files.FilesController
import siberia.modules.logger.controller.SystemEventController
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.logger.data.models.SystemEventObjectTypeModel
import siberia.modules.logger.data.models.SystemEventTypeModel
import siberia.modules.logger.service.SystemEventService
import siberia.modules.product.controller.ProductController
import siberia.modules.product.controller.ProductGroupController
import siberia.modules.product.data.models.ProductModel
import siberia.modules.product.service.*
import siberia.modules.stock.data.models.StockModel
import siberia.modules.stock.data.models.StockToProductModel
import siberia.modules.rbac.controller.RbacController
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.user.controller.UserController
import siberia.modules.user.data.models.UserModel
import siberia.modules.rbac.service.RbacService
import siberia.modules.rbac.service.RoleEventService
import siberia.modules.stock.controller.StockController
import siberia.modules.stock.service.StockEventService
import siberia.modules.stock.service.StockService
import siberia.modules.transaction.controller.*
import siberia.modules.transaction.data.models.*
import siberia.modules.transaction.service.*
import siberia.modules.user.service.UserAccessControlService
import siberia.modules.user.service.UserEventService
import siberia.modules.user.service.UserService
import siberia.modules.user.service.UserSocketService
import siberia.plugins.*
import siberia.utils.database.DatabaseConnector
import siberia.utils.kodein.bindSingleton
import siberia.utils.kodein.kodeinApplication
import siberia.utils.websockets.WebSocketRegister

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
        bindSingleton { AuthQrService(it) }
        bindSingleton { UserService(it) }
        bindSingleton { UserEventService(it) }
        bindSingleton { UserAccessControlService(it) }
        bindSingleton { UserSocketService(it) }
        bindSingleton { RbacService(it) }
        bindSingleton { RoleEventService(it) }
        bindSingleton { SystemEventService(it) }
        bindSingleton { BrandService(it) }
        bindSingleton { BrandEventService(it) }
        bindSingleton { CollectionService(it) }
        bindSingleton { CollectionEventService(it) }
        bindSingleton { CategoryService(it) }
        bindSingleton { CategoryEventService(it) }
        bindSingleton { ProductService(it) }
        bindSingleton { ProductEventService(it) }
        bindSingleton { ProductParseService(it) }
        bindSingleton { StockService(it) }
        bindSingleton { StockEventService(it) }
        bindSingleton { TransactionService(it) }
        bindSingleton { IncomeTransactionService(it) }
        bindSingleton { OutcomeTransactionService(it) }
        bindSingleton { TransferTransactionService(it) }
        bindSingleton { WriteOffTransactionService(it) }
        bindSingleton { WebSocketRegister(it) }
        bindSingleton { AuthSocketService(it) }
        bindSingleton { ProductGroupService(it) }
        bindSingleton { ProductGroupEventService(it) }
        bindSingleton { BugReportService(it) }


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
        bindSingleton { IncomeTransactionController(it) }
        bindSingleton { OutcomeTransactionController(it) }
        bindSingleton { TransferTransactionController(it) }
        bindSingleton { WriteOffTransactionController(it) }
        bindSingleton { FilesController(it) }
        bindSingleton { ProductGroupController(it) }
        bindSingleton { BugReportController(it) }
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
        BugReportModel
    ) {
        DatabaseInitializer.initRules()
        DatabaseInitializer.initEventTypes()
        DatabaseInitializer.initObjectTypes()
        DatabaseInitializer.initRequestTypes()
        DatabaseInitializer.initRequestStatuses()
        DatabaseInitializer.initUsers()
        DatabaseInitializer.initCategory()
        DatabaseInitializer.initTestData()
        commit()
    }
}
