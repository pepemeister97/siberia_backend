package siberia.conf

import io.ktor.util.date.*
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import siberia.modules.brand.data.models.BrandModel
import siberia.modules.category.data.models.CategoryModel
import siberia.modules.category.data.models.CategoryToCategoryModel
import siberia.modules.collection.data.models.CollectionModel
import siberia.modules.logger.data.models.SystemEventObjectTypeModel
import siberia.modules.logger.data.models.SystemEventTypeModel
import siberia.modules.notifications.data.models.NotificationDomainModel
import siberia.modules.notifications.data.models.NotificationTypeModel
import siberia.modules.product.data.models.ProductModel
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.rbac.data.models.rule.RuleModel
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.models.TransactionStatusModel
import siberia.modules.transaction.data.models.TransactionTypeModel
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.transaction
import siberia.utils.security.bcrypt.CryptoUtil

object DatabaseInitializer {
    fun initRules() {
        if (!RuleModel.selectAll().empty())
            return
        RuleModel.insert {
            it[id] = AppConf.rules.userManaging
            it[name] = "User managing"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
        }
        RuleModel.insert {
            it[id] = AppConf.rules.rbacManaging
            it[name] = "Rbac managing"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
        }
        RuleModel.insert {
            it[id] = AppConf.rules.checkLogs
            it[name] = "Check logs"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
        }
        RuleModel.insert {
            it[id] = AppConf.rules.brandManaging
            it[name] = "Brand managing"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
        }
        RuleModel.insert {
            it[id] = AppConf.rules.collectionManaging
            it[name] = "Collection managing"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
        }
        RuleModel.insert {
            it[id] = AppConf.rules.categoryManaging
            it[name] = "Category managing"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
        }
        RuleModel.insert {
            it[id] = AppConf.rules.productsManaging
            it[name] = "Products managing"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
        }
        RuleModel.insert {
            it[id] = AppConf.rules.stockManaging
            it[name] = "Stock managing"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
        }

        RuleModel.insert {
            it[id] = AppConf.rules.createIncomeRequest
            it[name] = "Create income requests"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }
        RuleModel.insert {
            it[id] = AppConf.rules.approveIncomeRequest
            it[name] = "Approve income requests"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }

        RuleModel.insert {
            it[id] = AppConf.rules.createOutcomeRequest
            it[name] = "Create outcome requests"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }
        RuleModel.insert {
            it[id] = AppConf.rules.approveOutcomeRequest
            it[name] = "Approve outcome requests"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }

        RuleModel.insert {
            it[id] = AppConf.rules.createTransferRequest
            it[name] = "Create transfer request"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }
        RuleModel.insert {
            it[id] = AppConf.rules.approveTransferRequestCreation
            it[name] = "Approve transfer request creation"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }
        RuleModel.insert {
            it[id] = AppConf.rules.manageTransferRequest
            it[name] = "Manage transfer request"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }
        RuleModel.insert {
            it[id] = AppConf.rules.approveTransferDelivery
            it[name] = "Approve transfer delivery"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }
        RuleModel.insert {
            it[id] = AppConf.rules.solveNotDeliveredProblem
            it[name] = "Solve not delivered problem"
            it[description] = "Ability to create new users and manage their rules, roles and personal info"
            it[needStock] = true
        }

    }

    fun initEventTypes() {
        if (!SystemEventTypeModel.selectAll().empty())
            return
        SystemEventTypeModel.insert {
            it[id] = AppConf.eventTypes.createEvent
            it[name] = "Creation"
        }
        SystemEventTypeModel.insert {
            it[id] = AppConf.eventTypes.updateEvent
            it[name] = "Update"
        }
        SystemEventTypeModel.insert {
            it[id] = AppConf.eventTypes.removeEvent
            it[name] = "Remove"
        }
    }

    fun initObjectTypes() {
        if (!SystemEventObjectTypeModel.selectAll().empty())
            return
        SystemEventObjectTypeModel.insert {
            it[id] = AppConf.objectTypes.userEvent
            it[name] = "User event"
        }
        SystemEventObjectTypeModel.insert {
            it[id] = AppConf.objectTypes.stockEvent
            it[name] = "Stock event"
        }
        SystemEventObjectTypeModel.insert {
            it[id] = AppConf.objectTypes.roleEvent
            it[name] = "Role event"
        }
        SystemEventObjectTypeModel.insert {
            it[id] = AppConf.objectTypes.productEvent
            it[name] = "Product event"
        }
        SystemEventObjectTypeModel.insert {
            it[id] = AppConf.objectTypes.brandEvent
            it[name] = "Brand event"
        }
        SystemEventObjectTypeModel.insert {
            it[id] = AppConf.objectTypes.collectionEvent
            it[name] = "Collection event"
        }
        SystemEventObjectTypeModel.insert {
            it[id] = AppConf.objectTypes.categoryEvent
            it[name] = "Category event"
        }
        SystemEventObjectTypeModel.insert {
            it[id] = AppConf.objectTypes.transactionEvent
            it[name] = "Transaction event"
        }
    }

    fun initRequestTypes() {
        if (!TransactionTypeModel.selectAll().empty())
            return
        TransactionTypeModel.insert {
            it[id] = AppConf.requestTypes.income
            it[name] = "Income"
        }
        TransactionTypeModel.insert {
            it[id] = AppConf.requestTypes.outcome
            it[name] = "Outcome"
        }
        TransactionTypeModel.insert {
            it[id] = AppConf.requestTypes.transfer
            it[name] = "Transfer"
        }
    }

    fun initRequestStatuses() {
        if (!TransactionStatusModel.selectAll().empty())
            return
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.open
            it[name] = "Open"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.created
            it[name] = "Created"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.creationCancelled
            it[name] = "Creation cancelled"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.inProgress
            it[name] = "In progress"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.processingCancelled
            it[name] = "Processing cancelled"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.delivered
            it[name] = "Delivered"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.notDelivered
            it[name] = "Not delivered"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.failed
            it[name] = "Failed"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.processed
            it[name] = "Processed"
        }
        TransactionStatusModel.insert {
            it[id] = AppConf.requestStatus.deliveryCancelled
            it[name] = "Delivery cancelled"
        }
    }

    fun initNotificationTypes() {
        if (!NotificationTypeModel.selectAll().empty())
            return
        NotificationTypeModel.insert {
            it[id] = AppConf.notificationTypes.info
            it[name] = "Info"
        }
        NotificationTypeModel.insert {
            it[id] = AppConf.notificationTypes.warn
            it[name] = "Warn"
        }
        NotificationTypeModel.insert {
            it[id] = AppConf.notificationTypes.critical
            it[name] = "Critical"
        }
    }

    fun initNotificationDomains() {
        if (!NotificationDomainModel.selectAll().empty())
            return
        NotificationDomainModel.insert {
            it[id] = AppConf.notificationDomains.transactions
            it[name] = "Transaction"
        }
    }

    fun initCategory() {
        if (!CategoryModel.selectAll().empty())
            return
        CategoryModel.insert {
            it[id] = 1
            it[name] = "Root"
        }
    }

    fun initUsers() {
        if (!UserModel.selectAll().empty())
            return
        UserModel.insert {
            it[id] = 1
            it[name] = "Andréas Podrochitté"
            it[login] = "podrochitte"
            it[hash] = CryptoUtil.hash("andreas")
            it[lastLogin] = getTimeMillis()
        }
        UserModel.insert {
            it[id] = 2
            it[name] = "Pablo Él Huanitto"
            it[login] = "huanitto"
            it[hash] = CryptoUtil.hash("pablo")
            it[lastLogin] = getTimeMillis()
        }

        if (!RoleModel.selectAll().empty())
            return

        val roleId = 1

        RoleModel.insert {
            it[id] = roleId
            it[name] = "SuperAdmin-AUTOGENERATED"
            it[description] = "Autogenerated role with full access to the system"
        }

        RbacModel.insert {
            it[user] = 1
            it[role] = roleId
        }

        RbacModel.insert {
            it[user] = 2
            it[role] = roleId
        }

        val rules = listOf<Int>(
            AppConf.rules.userManaging,
            AppConf.rules.rbacManaging,
            AppConf.rules.checkLogs,
            AppConf.rules.brandManaging,
            AppConf.rules.collectionManaging,
            AppConf.rules.categoryManaging,
            AppConf.rules.productsManaging,
            AppConf.rules.stockManaging,
        )

        val linkedRules = rules.map {
            LinkedRuleOutputDto(it)
        }

        RbacModel.batchInsert(rules) {
            this[RbacModel.role] = roleId
            this[RbacModel.rule] = it
        }

        RbacModel.expandAppendedRules(roleId, linkedRules)
    }

    fun initTestData() = transaction {
        val items = listOf<Int>(2, 3, 4)
        var createProducts = true
        if (BrandModel.selectAll().empty()) {
            BrandModel.batchInsert(items) {
                this[BrandModel.id] = it
                this[BrandModel.name] = "Brand #$it"
            }
        }
        else
            createProducts = false
        if (CollectionModel.selectAll().empty()) {
            CollectionModel.batchInsert(items) {
                this[CollectionModel.id] = it
                this[CollectionModel.name] = "Collection #$it"
            }
        }
        else
            createProducts = false
        if (CategoryModel.selectAll().count() <= 1) {
            CategoryModel.insert {
                it[id] = items[0]
                it[name] = "Category #${items[0]}"
            }
            CategoryModel.insert {
                it[id] = items[1]
                it[name] = "Sub Category #${items[1]}"
            }
            CategoryModel.insert {
                it[id] = items[2]
                it[name] = "Sub Sub Category #${items[2]}"
            }
            CategoryToCategoryModel.insert {
                it[parent] = 1
                it[child] = items[0]
            }
            CategoryToCategoryModel.insert {
                it[parent] = items[0]
                it[child] = items[1]
            }
            CategoryToCategoryModel.insert {
                it[parent] = items[1]
                it[child] = items[2]
            }
        }
        else
            createProducts = false
        commit()

        if (ProductModel.selectAll().empty() && createProducts) {
            ProductModel.batchInsert(items) {
                this[ProductModel.id] = it
                this[ProductModel.photo] = "$it.png"
                this[ProductModel.vendorCode] = getTimeMillis().toString()
                this[ProductModel.barcode] = getTimeMillis().toString()
                this[ProductModel.brand] = it
                this[ProductModel.name] = "Product #$it"
                this[ProductModel.description] = "Description for product #$it"
                this[ProductModel.purchasePrice] = (it * 8).toDouble()
                this[ProductModel.distributorPrice] = (it * 16).toDouble()
                this[ProductModel.professionalPrice] = (it * 24).toDouble()
                this[ProductModel.commonPrice] = (it * 32).toDouble()
                this[ProductModel.category] = it
                this[ProductModel.collection] = it
                this[ProductModel.color] = "New ultra color #$it"
                this[ProductModel.amountInBox] = it
                this[ProductModel.expirationDate] = it * 50000000L
                this[ProductModel.link] = "https://fashion-is-my-profession/$it"
            }
        }
        if (StockModel.selectAll().empty()) {
            StockModel.batchInsert(items) {
                this[StockModel.id] = it
                this[StockModel.name] = "Description for auto-generated stock #$it"
                this[StockModel.address] = "$it Podrochitte ave."
            }
            val roleId = 1
            if (
                !RoleModel.select { RoleModel.id eq roleId }.empty() &&
                !UserModel.select { UserModel.id eq 1 }.empty() &&
                !UserModel.select { UserModel.id eq 2 }.empty()
            ) {
                val rules = listOf(
                    Pair(9, 2),
                    Pair(10, 2),
                    Pair(11, 2),
                    Pair(12, 2),
                    Pair(13, 2),
                    Pair(14, 2),
                    Pair(15, 2),
                    Pair(16, 2),
                    Pair(17, 2),
                    Pair(9, 3),
                    Pair(10, 3),
                    Pair(11, 3),
                    Pair(12, 3),
                    Pair(13, 3),
                    Pair(14, 3),
                    Pair(15, 3),
                    Pair(16, 3),
                    Pair(17, 3),
                )
                RbacModel.batchInsert(rules) {
                    this[RbacModel.role] = roleId
                    this[RbacModel.rule] = it.first
                    this[RbacModel.stock] = it.second
                }
                RbacModel.expandAppendedRules(roleId, rules.map { LinkedRuleOutputDto(it.first, stockId = it.second, needStock = true) })
            }
        }
    }
}