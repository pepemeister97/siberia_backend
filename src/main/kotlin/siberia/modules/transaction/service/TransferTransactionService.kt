package siberia.modules.transaction.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.data.dto.TransactionOutputDto
import siberia.modules.user.data.dao.UserDao
import siberia.plugins.Logger
import siberia.utils.database.idValue

class TransferTransactionService(di: DI) : AbstractTransactionService(di) {
    private fun checkTypeAndChangeStatus(
        authorizedUser: AuthorizedUser,
        transactionDao: TransactionDao,
        targetStockId: Int,
        statusId: Int
    ): TransactionDao = transaction {
        if (transactionDao.typeId != AppConf.requestTypes.transfer)
            throw ForbiddenException()

        changeStatusTo(
            authorizedUser,
            transactionDao.idValue,
            targetStockId,
            statusId
        )
    }
    fun create(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto): TransactionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val targetStockId = transactionInputDto.to ?: throw BadRequestException("Incorrect target stock")

        val transactionDao = createTransaction(userDao, transactionInputDto, targetStockId)

        commit()

        try {
            approveCreation(authorizedUser, transactionDao.idValue)
        } catch (_: Exception) {
            transactionDao.toOutputDto()
        }
    }

    fun approveCreation(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.transfer)
            throw ForbiddenException()

        changeStatusTo(
            authorizedUser,
            transactionId,
            transactionDao.toId ?: throw BadRequestException("Bad transaction"),
            AppConf.requestStatus.open
        ).toOutputDto()
    }

    fun cancelCreation(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        val targetStockId = transactionDao.toId ?: throw BadRequestException("Bad transaction")

        checkTypeAndChangeStatus(
            authorizedUser,
            transactionDao,
            targetStockId,
            AppConf.requestStatus.creationCancelled
        ).toOutputDto()
    }

    fun startProcess(authorizedUser: AuthorizedUser, transactionId: Int, processByStock: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        StockDao[processByStock]

        val transactionInProgress = checkTypeAndChangeStatus(
            authorizedUser,
            transactionDao,
            processByStock,
            AppConf.requestStatus.inProgress
        )

        StockModel.removeProducts(processByStock, transactionInProgress.inputProductsList)

        transactionInProgress.toOutputDto()
    }

    fun cancelProcess(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        val processingStockId = transactionDao.fromId ?: throw BadRequestException("Bad transaction")

        val cancelledTransaction = checkTypeAndChangeStatus(
            authorizedUser,
            transactionDao,
            processingStockId,
            AppConf.requestStatus.processingCancelled
        )

        StockModel.appendProducts(processingStockId, transactionDao.inputProductsList)

        cancelledTransaction.toOutputDto()
    }

    fun delivered(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        val targetStockId = transactionDao.toId ?: throw BadRequestException("Bad transaction")

        val deliveredTransaction = checkTypeAndChangeStatus(
            authorizedUser,
            transactionDao,
            targetStockId,
            AppConf.requestStatus.delivered
        )

        StockModel.appendProducts(targetStockId, transactionDao.inputProductsList)

        deliveredTransaction.toOutputDto()
    }

    //TODO: Mechanism for partial delivering
    fun partialDelivered(
        authorizedUser: AuthorizedUser,
        transactionId: Int,
        productsDelivered: List<TransactionInputDto.TransactionProductInputDto>
    ): TransactionOutputDto = transaction {
        Logger.debug("Products partial delivered", "main")
        Logger.debug(productsDelivered, "main")
        delivered(authorizedUser, transactionId)
    }

    fun notDelivered(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        val targetStockId = transactionDao.toId ?: throw BadRequestException("Bad transaction")

        checkTypeAndChangeStatus(
            authorizedUser,
            transactionDao,
            targetStockId,
            AppConf.requestStatus.notDelivered
        ).toOutputDto()
    }

    fun solveNotDelivered(authorizedUser: AuthorizedUser, transactionId: Int, solveStatus: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.transfer)
            throw ForbiddenException()
        val targetStockId = when (solveStatus) {
            AppConf.requestStatus.failed -> {
                null
            }
            AppConf.requestStatus.delivered -> {
                transactionDao.toId ?: throw BadRequestException("Bad transaction")
            }
            AppConf.requestStatus.deliveryCancelled -> {
                transactionDao.fromId ?: throw BadRequestException("Bad transaction")
            }
            else -> {
                throw BadRequestException("Bad status provided")
            }
        }

        if (targetStockId != null)
            StockModel.appendProducts(targetStockId, transactionDao.inputProductsList)

        changeStatusTo(
            authorizedUser,
            transactionId,
            transactionDao.fromId ?: 0,
            solveStatus,
            checkRules = false
        ).toOutputDto()
    }
}