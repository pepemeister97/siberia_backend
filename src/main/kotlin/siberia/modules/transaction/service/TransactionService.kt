package siberia.modules.transaction.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dao.TransactionStatusDao
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.data.dto.TransactionOutputDto
import siberia.modules.transaction.data.dto.systemevents.TransactionCreateEvent
import siberia.modules.transaction.data.dto.systemevents.TransactionUpdateStatusEvent
import siberia.modules.transaction.data.models.TransactionModel
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.service.UserAccessControlService
import siberia.utils.database.idValue
import siberia.utils.database.transaction
import siberia.utils.kodein.KodeinService

class TransactionService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()

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
                    AppConf.requestStatus.processed -> {
                        AppConf.rules.approveIncomeRequest
                    }
                    else -> {
                        throw Exception("Bad request status")
                    }
                }
            }
            AppConf.requestTypes.outcome -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createOutcomeRequest
                    }
                    AppConf.requestStatus.processed -> {
                        AppConf.rules.approveOutcomeRequest
                    }
                    else -> {
                        throw Exception("Bad request status")
                    }
                }
            }
            AppConf.requestTypes.transfer -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createTransferRequest
                    }
                    AppConf.requestStatus.open -> {
                        AppConf.rules.approveTransferRequestCreation
                    }
                    AppConf.requestStatus.creationCancelled -> {
                        AppConf.rules.approveTransferRequestCreation
                    }
                    AppConf.requestStatus.inProgress -> {
                        AppConf.rules.manageTransferRequest
                    }
                    AppConf.requestStatus.processingCancelled -> {
                        AppConf.rules.manageTransferRequest
                    }
                    AppConf.requestStatus.delivered -> {
                        AppConf.rules.approveTransferDelivery
                    }
                    AppConf.requestStatus.notDelivered -> {
                        AppConf.rules.approveTransferDelivery
                    }
                    else -> {
                        throw Exception("Bad request status")
                    }
                }
            }
            else -> {
                throw Exception("Bad request type")
            }
        }

    //If transfer returns "to" to easier checking access
    private fun getTargetStock(transactionInputDto: TransactionInputDto): Int =
        when (transactionInputDto.type) {
            AppConf.requestTypes.income -> transactionInputDto.to
            AppConf.requestTypes.outcome -> transactionInputDto.from
            AppConf.requestTypes.transfer -> transactionInputDto.to
            else -> throw Exception("Bad request type")
        }!!

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

    private fun changeTransactionStatus(userDao: UserDao, transactionId: Int, statusId: Int): TransactionDao = transaction {
        val transactionDao = TransactionDao[transactionId]
        val targetStock = StockDao[getTargetStock(transactionDao.toInputDto())]

        val ruleToApprove = mapTypeToRule(transactionDao.typeId, statusId)

        if (!userAccessControlService.checkAccessToStock(userDao.idValue, ruleToApprove, targetStock.idValue))
            throw ForbiddenException()

        transactionDao.status = TransactionStatusDao[statusId]

        transactionDao
    }



    //Income / Outcome transactions
    private fun approveIncomeOutcomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionDao = transaction {
        val userDao = UserDao[authorizedUser.id]
        val statusDao = TransactionStatusDao[AppConf.requestStatus.processed]
        val transactionDao = changeTransactionStatus(userDao, transactionId, statusDao.idValue)
        val targetStock = StockDao[getTargetStock(transactionDao.toInputDto())]

        val products = TransactionModel.getFullProductList(transactionId)
        StockModel.appendProducts(targetStock.idValue, products)
        val event = TransactionUpdateStatusEvent(userDao.login, targetStock.name, transactionId, statusDao.name)
        SystemEventModel.logEvent(event)

        transactionDao
    }

    private fun cancelIncomeOutcomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionDao = transaction {
        val userDao = UserDao[authorizedUser.id]
        val statusDao = TransactionStatusDao[AppConf.requestStatus.creationCancelled]
        val transactionDao = changeTransactionStatus(userDao, transactionId, statusDao.idValue)
        val targetStock = StockDao[getTargetStock(transactionDao.toInputDto())]

        val event = TransactionUpdateStatusEvent(userDao.login, targetStock.name, transactionId, statusDao.name)
        SystemEventModel.logEvent(event)

        transactionDao
    }

    fun createIncomeTransaction(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto): TransactionOutputDto = transaction {
        val targetStock = StockDao[getTargetStock(transactionInputDto)]
        val incomeTransaction = createTransaction(authorizedUser, transactionInputDto, targetStock)

        commit()

        try {
            approveIncomeTransaction(authorizedUser, incomeTransaction.idValue)
        } catch (e: ForbiddenException) {
            incomeTransaction.toOutputDto()
        }
    }

    fun approveIncomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val approvedTransaction = approveIncomeOutcomeTransaction(authorizedUser, transactionId)
        commit()
        approvedTransaction.toOutputDto()
    }

    fun cancelIncomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val cancelledTransaction = cancelIncomeOutcomeTransaction(authorizedUser, transactionId)
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

        commit()

        try {
            approveOutcomeTransaction(authorizedUser, outcomeTransaction.idValue)
        } catch (e: ForbiddenException) {
            outcomeTransaction.toOutputDto()
        }
    }
    fun approveOutcomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val approvedTransaction = approveIncomeOutcomeTransaction(authorizedUser, transactionId)
        commit()
        approvedTransaction.toOutputDto()
    }

    fun cancelOutcomeTransaction(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val cancelledTransaction = cancelIncomeOutcomeTransaction(authorizedUser, transactionId)
        val targetStock = StockDao[getTargetStock(cancelledTransaction.toInputDto())]
        StockModel.removeProducts(targetStock.idValue, cancelledTransaction.fullOutput().products)
        commit()
        cancelledTransaction.toOutputDto()
    }



//    //Transfer transactions
//    fun createTransferTransaction(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto) = transaction {
//        val userDao = UserDao[authorizedUser.id]
//        val ruleToCreate = mapTypeToRule(transactionInputDto.type, AppConf.requestStatus.created)
//    }
}