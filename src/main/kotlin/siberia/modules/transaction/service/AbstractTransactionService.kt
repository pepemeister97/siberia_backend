package siberia.modules.transaction.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dao.TransactionStatusDao
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.data.dto.systemevents.TransactionCreateEvent
import siberia.modules.transaction.data.dto.systemevents.TransactionUpdateStatusEvent
import siberia.modules.transaction.data.models.TransactionModel
import siberia.modules.transaction.data.models.TransactionRelatedUserModel
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.service.UserAccessControlService
import siberia.plugins.Logger
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

abstract class AbstractTransactionService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()

    private fun checkAccessToStatusForTransaction(userDao: UserDao, transactionId: Int, targetStockId: Int, statusId: Int): Boolean {
        val transactionDao = TransactionDao[transactionId]
        Logger.debug(TransactionUtils.availableStatuses(transactionDao), "main")
        Logger.debug(statusId, "main")
        return if (TransactionUtils.availableStatuses(transactionDao).contains(statusId)) {
            val ruleId = TransactionUtils.mapTypeToRule(transactionDao.typeId, statusId)
            Logger.debug(ruleId, "main")
            return userAccessControlService.checkAccessToStock(userDao.idValue, ruleId, targetStockId)
        }
        else
            false
    }

    protected fun createTransaction(userDao: UserDao, transactionInputDto: TransactionInputDto, targetStockId: Int): TransactionDao = transaction {
        val targetStock = StockDao[targetStockId]
        val ruleToCreate = TransactionUtils.mapTypeToRule(transactionInputDto.type, AppConf.requestStatus.created)

        if (!userAccessControlService.checkAccessToStock(userDao.idValue, ruleToCreate, targetStock.idValue))
            throw ForbiddenException()

        val transactionDao = TransactionModel.create(transactionInputDto)
        val event = TransactionCreateEvent(userDao.login, targetStock.name, transactionDao.idValue)
        SystemEventModel.logEvent(event)
        TransactionRelatedUserModel.addRelated(userDao.idValue, transactionDao.idValue)

        transactionDao
    }

    protected fun changeStatusTo(authorizedUser: AuthorizedUser, transactionId: Int, targetStockId: Int, statusId: Int, checkRules: Boolean = true): TransactionDao = transaction {
        val userDao = UserDao[authorizedUser.id]
        val transactionDao = TransactionDao[transactionId]
        val targetStock = StockDao[targetStockId]
        val statusDao = TransactionStatusDao[statusId]

        //If checkRules == false -> skip checking
        if (
            checkRules &&
            !checkAccessToStatusForTransaction(userDao, transactionId, targetStockId, statusId)
        )
            throw ForbiddenException()

        val event = TransactionUpdateStatusEvent(userDao.login, targetStock.name, transactionId, statusDao.name)
        SystemEventModel.logEvent(event)

        transactionDao.status = TransactionStatusDao[statusId]
        transactionDao.flush()
        TransactionRelatedUserModel.addRelated(userDao.idValue, transactionId)

        transactionDao
    }
}