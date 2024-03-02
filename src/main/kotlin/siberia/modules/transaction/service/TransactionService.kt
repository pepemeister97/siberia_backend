package siberia.modules.transaction.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.rbac.data.dao.RuleDao.Companion.createListCond
import siberia.modules.rbac.data.dao.RuleDao.Companion.createNullableListCond
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dao.TransactionStatusDao
import siberia.modules.transaction.data.dto.*
import siberia.modules.transaction.data.dto.status.TransactionStatusOutputDto
import siberia.modules.transaction.data.dto.type.TransactionTypeOutputDto
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.service.UserAccessControlService
import siberia.plugins.Logger
import siberia.utils.database.idValue
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.conf.AppConf.requestStatus
import siberia.exceptions.BadRequestException
import siberia.modules.product.data.dao.ProductDao
import siberia.modules.transaction.data.models.*
import siberia.utils.kodein.KodeinService

class TransactionService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()

    fun getAllTypes() = transaction { TransactionTypeModel.selectAll().map { TransactionTypeOutputDto(id = it[TransactionTypeModel.id].value, name = it[TransactionTypeModel.name]) } }

    fun getAllStatuses() = transaction { TransactionStatusModel.selectAll().map { TransactionStatusOutputDto(id = it[TransactionStatusModel.id].value, name = it[TransactionStatusModel.name]) } }

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

    private fun checkAccessToTransaction(authorizedUser: AuthorizedUser, transactionId: Int): Boolean = transaction {
        val transactionDao = TransactionDao[transactionId]
        val transactionStocks = listOfNotNull(
            transactionDao.to?.idValue, transactionDao.from?.idValue
        )
        userAccessControlService.filterAvailable(authorizedUser.id, transactionStocks).isNotEmpty() || transactionDao.typeId == AppConf.requestTypes.transfer
    }

    fun getAvailableTransactions(authorizedUser: AuthorizedUser, transactionSearchFilter: TransactionSearchFilter): List<TransactionListItemOutputDto> = transaction {
        val availableStocksWithRules = userAccessControlService.getAvailableStocksByOperations(authorizedUser.id)
        val availableStocks = availableStocksWithRules.map { it.key }
        //If status is OPEN it means that managers from all other stocks can see that request
        val processQuery = if (authorizedUser.rules.map { rule -> rule.ruleId }.contains(AppConf.rules.manageTransferRequest))
            TransactionModel.status eq requestStatus.open
        else
            TransactionModel.to inList availableStocks
        TransactionModel.select {
            (
                    TransactionModel.hidden eq false and (
                    (TransactionModel.from inList availableStocks) or
                    (TransactionModel.to inList availableStocks) or
                    processQuery)
            ) and
            createNullableListCond(transactionSearchFilter.to, TransactionModel.id.isNotNull(), TransactionModel.to) and
            createNullableListCond(transactionSearchFilter.from, TransactionModel.id.isNotNull(), TransactionModel.from) and
            createListCond(transactionSearchFilter.status, TransactionModel.id.isNotNull(), TransactionModel.status) and
            createListCond(transactionSearchFilter.type, TransactionModel.id.isNotNull(), TransactionModel.type)
        }.orderBy(Pair(TransactionModel.updatedAt, SortOrder.DESC_NULLS_FIRST), Pair(TransactionModel.status, SortOrder.ASC)).map {
            TransactionDao.wrapRow(it).listItemOutputDto
        }
    }

    fun getAvailableStatuses(authorizedUser: AuthorizedUser, transactionId: Int): List<TransactionStatusOutputDto> = transaction {
        val userDao = UserDao[authorizedUser.id]
        val transactionDao = TransactionDao[transactionId]
        if (!checkAccessToTransaction(authorizedUser, transactionId))
            throw ForbiddenException()
        val hasAccessToManageTransfers = userDao.hasAccessToProcessTransfers
        Logger.debug("HAS ACCESS TO MANAGE", "main")
        Logger.debug(hasAccessToManageTransfers, "main")
        TransactionUtils.availableStatuses(transactionDao).filter {
            if (
                transactionDao.statusId == requestStatus.open
                && transactionDao.typeId == AppConf.requestTypes.transfer
                && hasAccessToManageTransfers
            )
                true
            else if (it == requestStatus.inProgress && !hasAccessToManageTransfers)
                false
            else {
                val ruleId = TransactionUtils.mapTypeToRule(transactionDao.typeId, it)
                Logger.debug(transactionDao.idValue, "main")
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

    fun getTransactionOnAssembly(authorizedUser: AuthorizedUser): List<TransactionListItemOutputDto> = transaction {
        val availableStocksWithRules = userAccessControlService.getAvailableStocksByOperations(authorizedUser.id)
        val availableStocks = availableStocksWithRules.map { it.key }
        TransactionModel.select {
            (TransactionModel.from inList availableStocks) and
            (TransactionModel.status eq requestStatus.open) and
            (TransactionModel.type eq AppConf.requestTypes.outcome) and
            (TransactionModel.hidden eq false)
        }.sortedBy { TransactionModel.updatedAt }.map {
            TransactionDao.wrapRow(it).listItemOutputDto
        }
    }

    fun getProductsFromTransactions(authorizedUser: AuthorizedUser, transactionsList: List<Int>): List<TransactionFullOutputDto.TransactionProductDto> = transaction {
        val checkAccess = transactionsList.all { checkAccessToTransaction(authorizedUser, it) }
        if (!checkAccess)
            throw ForbiddenException()

        val productsMap: MutableMap<Int, TransactionFullOutputDto.TransactionProductDto> = mutableMapOf()

        TransactionToProductModel.select {
            TransactionToProductModel.transaction inList transactionsList
        }.map {
            val productId = it[TransactionToProductModel.product].value
            val productDao = ProductDao[productId]
            val transactionProductDto = TransactionFullOutputDto.TransactionProductDto(productDao.toOutputDto(), it[TransactionToProductModel.amount], it[TransactionToProductModel.price])
            if (productsMap.containsKey(productId))
                productsMap[productId]!!.amount += transactionProductDto.amount
            else
                productsMap[productId] = transactionProductDto
        }

        productsMap.values.toList()
    }

    fun getTransactions(authorizedUser: AuthorizedUser, transactionsList: List<Int>): List<TransactionFullOutputDto> = transaction {
        val checkAccess = transactionsList.all { checkAccessToTransaction(authorizedUser, it) }
        if (!checkAccess)
            throw ForbiddenException()

        transactionsList.map { TransactionDao[it].fullOutput() }
    }

    fun getTransactionForQr(authorizedUser: AuthorizedUser, transactionId: Int): TransactionDao = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (!checkAccessToTransaction(authorizedUser, transactionId))
            throw ForbiddenException()

        if (!listOf(
            requestStatus.inProgress
        ).contains(transactionDao.statusId))
            throw ForbiddenException()

        transactionDao
    }
}