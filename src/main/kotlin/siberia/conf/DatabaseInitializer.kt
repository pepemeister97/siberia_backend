package siberia.conf

import io.ktor.util.date.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import siberia.modules.logger.data.models.SystemEventObjectTypeModel
import siberia.modules.logger.data.models.SystemEventTypeModel
import siberia.modules.notifications.data.models.NotificationDomainModel
import siberia.modules.notifications.data.models.NotificationTypeModel
import siberia.modules.rbac.data.models.rule.RuleModel
import siberia.modules.transaction.data.models.TransactionStatusModel
import siberia.modules.transaction.data.models.TransactionTypeModel
import siberia.modules.user.data.models.UserModel
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

    }
}