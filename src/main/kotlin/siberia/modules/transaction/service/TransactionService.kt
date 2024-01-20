package siberia.modules.transaction.service

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.notifications.service.NotificationService
import siberia.modules.product.service.ProductService
import siberia.modules.rbac.data.dao.RuleDao.Companion.createListCond
import siberia.modules.rbac.data.dao.RuleDao.Companion.createNullableListCond
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dao.TransactionStatusDao
import siberia.modules.transaction.data.dto.*
import siberia.modules.transaction.data.dto.status.TransactionStatusOutputDto
import siberia.modules.transaction.data.dto.systemevents.TransactionCreateEvent
import siberia.modules.transaction.data.dto.systemevents.TransactionUpdateStatusEvent
import siberia.modules.transaction.data.dto.type.TransactionTypeOutputDto
import siberia.modules.transaction.data.models.TransactionModel
import siberia.modules.transaction.data.models.TransactionRelatedUserModel
import siberia.modules.transaction.data.models.TransactionStatusModel
import siberia.modules.transaction.data.models.TransactionTypeModel
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.service.UserAccessControlService
import siberia.plugins.Logger
import siberia.utils.database.idValue
import siberia.utils.database.transaction
import siberia.utils.kodein.KodeinService

class TransactionService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()
    private val notificationService: NotificationService by instance()
    private val productService: ProductService by instance()

    fun getAllTypes() = transaction { TransactionTypeModel.selectAll().map { TransactionTypeOutputDto(id = it[TransactionTypeModel.id].value, name = it[TransactionTypeModel.name]) } }

    fun getAllStatuses() = transaction { TransactionStatusModel.selectAll().map { TransactionStatusOutputDto(id = it[TransactionStatusModel.id].value, name = it[TransactionStatusModel.name]) } }
    /*
        This method takes type of the request and status
        that user wants to set and returns the rules, which user must have to set corresponding status.
     */
    private fun mapTypeToRule(typeId: Int, statusId: Int): Int =
        when (typeId) {
            AppConf.requestTypes.income -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createIncomeRequest
                    }
                    AppConf.requestStatus.creationCancelled -> {
                        AppConf.rules.approveIncomeRequest
                    }
                    AppConf.requestStatus.processed -> {
                        AppConf.rules.approveIncomeRequest
                    }
                    else -> {
                        throw BadRequestException("Bad request status")
                    }
                }
            }
            AppConf.requestTypes.outcome -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createOutcomeRequest
                    }
                    AppConf.requestStatus.creationCancelled -> {
                        AppConf.rules.approveOutcomeRequest
                    }
                    AppConf.requestStatus.processed -> {
                        AppConf.rules.approveOutcomeRequest
                    }
                    else -> {
                        throw BadRequestException("Bad request status")
                    }
                }
            }
            AppConf.requestTypes.transfer -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createTransferRequest
                    }
                    AppConf.requestStatus.creationCancelled -> {
                        AppConf.rules.approveTransferRequestCreation
                    }
                    AppConf.requestStatus.open -> {
                        AppConf.rules.approveTransferRequestCreation
                    }
                    AppConf.requestStatus.processingCancelled -> {
                        AppConf.rules.manageTransferRequest
                    }
                    AppConf.requestStatus.inProgress -> {
                        AppConf.rules.manageTransferRequest
                    }
                    AppConf.requestStatus.delivered -> {
                        AppConf.rules.approveTransferDelivery
                    }
                    AppConf.requestStatus.notDelivered -> {
                        AppConf.rules.approveTransferDelivery
                    }
                    AppConf.requestStatus.failed -> {
                        AppConf.rules.solveNotDeliveredProblem
                    }
                    AppConf.requestStatus.deliveryCancelled -> {
                        AppConf.rules.solveNotDeliveredProblem
                    }
                    else -> {
                        throw BadRequestException("Bad request status")
                    }
                }
            }
            else -> {
                throw BadRequestException("Bad request type")
            }
        }

    //If transfer returns "to" to easier checking access
    private fun getTargetStock(transactionInputDto: TransactionInputDto): Int =
        when (transactionInputDto.type) {
            AppConf.requestTypes.income -> transactionInputDto.to
            AppConf.requestTypes.outcome -> transactionInputDto.from
            AppConf.requestTypes.transfer -> transactionInputDto.to
            else -> throw BadRequestException("Bad transaction type")
        } ?: throw BadRequestException("Bad transaction type")

    private fun getTargetStock(transactionDao: TransactionDao, statusId: Int): Int {
        val statusToStock = AppConf.requestToStockMapper[transactionDao.typeId]
        val stockPair = if (statusToStock != null)
                            statusToStock[statusId] ?: throw BadRequestException("Bad status")
                        else
                            throw BadRequestException("Bad type")

        Logger.debug(stockPair, "main")
        Logger.debug(statusId, "main")
        Logger.debug(transactionDao, "main")

        return (if (stockPair == AppConf.StockPair.TO)
                    transactionDao.to?.idValue
                else
                    transactionDao.from?.idValue
            ) ?: throw Exception("Bad transaction")
    }

    private fun availableStatuses(transactionDao: TransactionDao) = AppConf.requestStatusMapper[transactionDao.typeId]!![transactionDao.statusId] ?: listOf()

    private fun createTransaction(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto, targetStock: StockDao): TransactionDao = transaction {
        val userDao = UserDao[authorizedUser.id]
        val ruleToCreate = mapTypeToRule(transactionInputDto.type, AppConf.requestStatus.created)


        if (!userAccessControlService.checkAccessToStock(userDao.idValue, ruleToCreate, targetStock.idValue))
            throw ForbiddenException()

        val transactionDao = TransactionModel.create(transactionInputDto)
        val event = TransactionCreateEvent(userDao.login, targetStock.name, transactionDao.idValue)
        SystemEventModel.logEvent(event)

        transactionDao
    }

    private fun checkAccessToStatusForTransaction(userDao: UserDao, transactionId: Int, statusId: Int): Boolean {
        val transactionDao = TransactionDao[transactionId]
        return if (availableStatuses(transactionDao).contains(statusId)) {
            val ruleId = mapTypeToRule(transactionDao.typeId, statusId)
            val targetStock = getTargetStock(transactionDao, statusId)
            return userAccessControlService.checkAccessToStock(userDao.idValue, ruleId, targetStock)
        }
        else
            false
    }

    private fun changeTransactionStatus(userDao: UserDao, transactionId: Int, statusId: Int, checkRule: Boolean = true): TransactionDao = transaction {
        val transactionDao = TransactionDao[transactionId]

        if (
            checkRule &&
            !checkAccessToStatusForTransaction(userDao, transactionId, statusId)
        )
            throw ForbiddenException()

        transactionDao.status = TransactionStatusDao[statusId]
        transactionDao.flush()
        TransactionRelatedUserModel.addRelated(userDao.idValue, transactionId)

        transactionDao
    }



    //Income / Outcome transactions
    private fun approveIncomeOutcomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionDao = transaction {
        val userDao = UserDao[authorizedUser.id]
        var transactionDao = TransactionDao[transactionId]
        val targetStock = StockDao[getTargetStock(transactionDao.toInputDto())]
        val statusDao = TransactionStatusDao[AppConf.requestStatus.processed]
        transactionDao = changeTransactionStatus(userDao, transactionId, statusDao.idValue)

        val event = TransactionUpdateStatusEvent(userDao.login, targetStock.name, transactionId, statusDao.name)
        SystemEventModel.logEvent(event)

        transactionDao
    }

    private fun cancelIncomeOutcomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionDao = transaction {
        val userDao = UserDao[authorizedUser.id]
        val statusDao = TransactionStatusDao[AppConf.requestStatus.creationCancelled]
        var transactionDao = TransactionDao[transactionId]
        val targetStock = StockDao[getTargetStock(transactionDao.toInputDto())]
        transactionDao = changeTransactionStatus(userDao, transactionId, statusDao.idValue)

        val event = TransactionUpdateStatusEvent(userDao.login, targetStock.name, transactionId, statusDao.name)
        SystemEventModel.logEvent(event)

        transactionDao
    }

    fun createIncomeTransaction(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto): TransactionOutputDto = transaction {
        val targetStock = StockDao[getTargetStock(transactionInputDto)]
        val incomeTransaction = createTransaction(authorizedUser, transactionInputDto, targetStock)
        TransactionRelatedUserModel.addRelated(authorizedUser.id, incomeTransaction.idValue)

        commit()

        try {
            approveIncomeTransaction(authorizedUser, incomeTransaction.idValue)
        } catch (e: ForbiddenException) {
            incomeTransaction.toOutputDto()
        }
    }

    fun approveIncomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.income)
            throw ForbiddenException()
        val approvedTransaction = approveIncomeOutcomeTransaction(authorizedUser, transactionId)
        val targetStock = StockDao[getTargetStock(transactionDao.toInputDto())]
        val products = TransactionModel.getFullProductList(transactionId)
        productService.updateLastPurchaseData(products, approvedTransaction.updatedAt ?: approvedTransaction.createdAt)
        StockModel.appendProducts(targetStock.idValue, products.map { TransactionInputDto.TransactionProductInputDto(it.product.id, it.amount) })
        notificationService.notifyTransactionStatusChange(transactionId, approvedTransaction.statusId)
        commit()
        approvedTransaction.toOutputDto()
    }

    fun cancelIncomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.income)
            throw ForbiddenException()
        val cancelledTransaction = cancelIncomeOutcomeTransaction(authorizedUser, transactionId)
        notificationService.notifyTransactionStatusChange(transactionId, cancelledTransaction.statusId)
        commit()
        cancelledTransaction.toOutputDto()
    }

    fun createOutcomeTransaction(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto): TransactionOutputDto = transaction {
        val targetStock = StockDao[getTargetStock(transactionInputDto)]
        try {
            StockModel.checkAvailableAmount(targetStock.idValue, transactionInputDto.products)
        } catch (e: Exception) {
            throw BadRequestException("Not enough products in stock")
        }

        val outcomeTransaction = createTransaction(authorizedUser, transactionInputDto, targetStock)
        StockModel.removeProducts(targetStock.idValue, transactionInputDto.products)
        TransactionRelatedUserModel.addRelated(authorizedUser.id, outcomeTransaction.idValue)

        commit()

        try {
            approveOutcomeTransaction(authorizedUser, outcomeTransaction.idValue)
        } catch (e: ForbiddenException) {
            outcomeTransaction.toOutputDto()
        }
    }

    fun approveOutcomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.outcome)
            throw ForbiddenException()

        val approvedTransaction = approveIncomeOutcomeTransaction(authorizedUser, transactionId)
        notificationService.notifyTransactionStatusChange(transactionId, approvedTransaction.statusId)
        commit()
        approvedTransaction.toOutputDto()
    }

    fun cancelOutcomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.outcome)
            throw ForbiddenException()

        val cancelledTransaction = cancelIncomeOutcomeTransaction(authorizedUser, transactionId)
        val targetStock = StockDao[getTargetStock(cancelledTransaction.toInputDto())]
        StockModel.removeProducts(targetStock.idValue, cancelledTransaction.inputProductsList)
        notificationService.notifyTransactionStatusChange(transactionId, cancelledTransaction.statusId)
        commit()
        cancelledTransaction.toOutputDto()
    }



    //Transfer transactions
    fun createTransferTransaction(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto): TransactionOutputDto = transaction {
        val createdTransaction = createTransaction(authorizedUser, transactionInputDto, StockDao[getTargetStock(transactionInputDto)])
        try {
            approveTransferTransactionCreation(authorizedUser, createdTransaction.idValue)
        } catch (e: ForbiddenException) {
            commit()
            createdTransaction.toOutputDto()
        }
    }

    fun approveTransferTransactionCreation(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.transfer)
            throw ForbiddenException()

        val transactionOutputDto = changeTransactionStatus(UserDao[authorizedUser.id], transactionId, AppConf.requestStatus.open).toOutputDto()
        notificationService.notifyTransactionStatusChange(transactionId, transactionOutputDto.status)
        commit()
        transactionOutputDto
    }

    fun cancelTransferTransactionCreation(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.transfer)
            throw ForbiddenException()

        val transactionOutputDto = changeTransactionStatus(UserDao[authorizedUser.id], transactionId, AppConf.requestStatus.creationCancelled).toOutputDto()
        notificationService.notifyTransactionStatusChange(transactionId, transactionOutputDto.status)
        commit()
        transactionOutputDto
    }

    fun startProcessTransferTransaction(authorizedUser: AuthorizedUser, transactionId: Int, stockId: Int): TransactionOutputDto = transaction {
        val stockDao = StockDao[stockId]
        var transactionDao = TransactionDao[transactionId]

        if (transactionDao.to == stockDao)
            throw ForbiddenException()

        transactionDao.from = stockDao
        transactionDao.flush()

        transactionDao = changeTransactionStatus(UserDao[authorizedUser.id], transactionId, AppConf.requestStatus.inProgress)
        if (transactionDao.typeId != AppConf.requestTypes.transfer)
            throw ForbiddenException()

        StockModel.removeProducts(stockDao.idValue, transactionDao.inputProductsList)
        transactionDao.flush()
        notificationService.notifyTransactionStatusChange(transactionId, transactionDao.statusId)
        commit()

        transactionDao.toOutputDto()
    }

    fun cancelProcessingTransferTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]

        val transactionDao = changeTransactionStatus(userDao, transactionId, AppConf.requestStatus.processingCancelled)
        if (transactionDao.typeId != AppConf.requestTypes.transfer)
            throw ForbiddenException()

        StockModel.appendProducts(transactionDao.fromId!!, transactionDao.inputProductsList)
        notificationService.notifyTransactionStatusChange(transactionId, transactionDao.statusId)
        commit()

        transactionDao.toOutputDto()
    }

    fun approveTransferDelivery(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]

        val transactionDao = changeTransactionStatus(userDao, transactionId, AppConf.requestStatus.delivered)
        if (transactionDao.typeId != AppConf.requestTypes.transfer)
            throw ForbiddenException()

        StockModel.appendProducts(transactionDao.toId!!, transactionDao.inputProductsList)
        notificationService.notifyTransactionStatusChange(transactionId, transactionDao.statusId)
        commit()

        transactionDao.toOutputDto()
    }

    fun markAsNotDelivered(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val transactionOutputDto = changeTransactionStatus(userDao, transactionId, AppConf.requestStatus.notDelivered).toOutputDto()
        notificationService.notifyTransactionStatusChange(transactionId, transactionOutputDto.status)
        commit()
        transactionOutputDto
    }

    fun solveNotDelivered(authorizedUser: AuthorizedUser, transactionId: Int, solveTo: Int): TransactionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        var transactionDao = TransactionDao[transactionId]
        val fromStock = transactionDao.from!!
        val toStock = transactionDao.to!!

        transactionDao = when (solveTo) {
            AppConf.requestStatus.delivered -> {
                StockModel.appendProducts(toStock.idValue, transactionDao.inputProductsList)
                changeTransactionStatus(userDao, transactionId, AppConf.requestStatus.delivered, false)
            }
            AppConf.requestStatus.failed -> {
                changeTransactionStatus(userDao, transactionId, AppConf.requestStatus.failed, false)
            }
            AppConf.requestStatus.deliveryCancelled -> {
                StockModel.appendProducts(fromStock.idValue, transactionDao.inputProductsList)
                changeTransactionStatus(userDao, transactionId, AppConf.requestStatus.deliveryCancelled, false)
            }
            else -> throw ForbiddenException()
        }
        notificationService.notifyTransactionStatusChange(transactionId, transactionDao.statusId)
        commit()

        transactionDao.toOutputDto()
    }

    private fun checkAccessToTransaction(authorizedUser: AuthorizedUser, transactionId: Int): Boolean = transaction {
        val transactionDao = TransactionDao[transactionId]
        val transactionStocks = listOfNotNull(
            transactionDao.to?.idValue, transactionDao.from?.idValue
        )
        userAccessControlService.filterAvailable(authorizedUser.id, transactionStocks).isNotEmpty() || transactionDao.typeId == AppConf.requestTypes.transfer
    }

    fun getAvailableTransactions(authorizedUser: AuthorizedUser, transactionSearchFilter: TransactionSearchFilter): List<TransactionListItemOutputDto> = transaction {
        val availableStocksWithRules = userAccessControlService.getAvailableStocks(authorizedUser.id)
        val availableStocks = availableStocksWithRules.map { it.key }
        //If status is OPEN it means that managers from all other stocks can see that request
        val processQuery = if (authorizedUser.rules.map { rule -> rule.ruleId }.contains(AppConf.rules.manageTransferRequest))
            TransactionModel.status eq AppConf.requestStatus.open
        else
            TransactionModel.to inList availableStocks
        TransactionModel.select {
            (
                    (TransactionModel.from inList availableStocks) or
                    (TransactionModel.to inList availableStocks) or
                    processQuery
            ) and
            createNullableListCond(transactionSearchFilter.to, TransactionModel.id.isNotNull(), TransactionModel.to) and
            createNullableListCond(transactionSearchFilter.from, TransactionModel.id.isNotNull(), TransactionModel.from) and
            createListCond(transactionSearchFilter.status, TransactionModel.id.isNotNull(), TransactionModel.status) and
            createListCond(transactionSearchFilter.type, TransactionModel.id.isNotNull(), TransactionModel.type)
        }.sortedBy { TransactionModel.updatedAt }.map {
            TransactionDao.wrapRow(it).listItemOutputDto
        }
    }

    fun getAvailableStatuses(authorizedUser: AuthorizedUser, transactionId: Int): List<TransactionStatusOutputDto> = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (!checkAccessToTransaction(authorizedUser, transactionId))
            throw ForbiddenException()
        availableStatuses(transactionDao).filter {
            if (transactionDao.statusId == AppConf.requestStatus.open && transactionDao.typeId == AppConf.requestTypes.transfer)
                true
            else {
                val ruleId = mapTypeToRule(transactionDao.typeId, it)
                val targetStock = getTargetStock(transactionDao, it)
                (userAccessControlService.checkAccessToStock(authorizedUser.id, ruleId, targetStock))
            }
        }.map { TransactionStatusDao[it].toOutputDto() }
    }

    fun getOne(authorizedUser: AuthorizedUser, transactionId: Int): TransactionFullOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (checkAccessToTransaction(authorizedUser, transactionId))
            transactionDao.fullOutput()
        else
            throw ForbiddenException()
    }
}