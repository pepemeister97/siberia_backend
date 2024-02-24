package siberia.modules.transaction.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.data.dto.TransactionOutputDto
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.idValue

class OutcomeTransactionService(di: DI) : AbstractTransactionService(di) {
    fun create(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto): TransactionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val targetStockId = transactionInputDto.from ?: throw BadRequestException("Incorrect target stock")
        try {
            StockModel.checkAvailableAmount(targetStockId, transactionInputDto.products)
        } catch (_: Exception) {
            throw BadRequestException("Not enough products in stock")
        }

        val transactionDao = createTransaction(userDao, transactionInputDto, targetStockId)
        StockModel.removeProducts(targetStockId, transactionInputDto.products)

        commit()

        changeStatusTo(
            authorizedUser,
            transactionDao.idValue,
            targetStockId,
            AppConf.requestStatus.open
        ).toOutputDto()
    }


    fun cancelCreation(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.income)
            throw ForbiddenException()
        val targetStockId = transactionDao.fromId ?: throw BadRequestException("Bad transaction")

        StockModel.appendProducts(targetStockId, transactionDao.inputProductsList)

        changeStatusTo(
            authorizedUser,
            transactionId,
            targetStockId,
            AppConf.requestStatus.creationCancelled
        ).toOutputDto()
    }

    fun processed(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.outcome)
            throw ForbiddenException()

        val approvedTransaction = changeStatusTo(
            authorizedUser,
            transactionId,
            transactionDao.fromId ?: throw BadRequestException("Bad transaction"),
            AppConf.requestStatus.processed
        )

        commit()

        approvedTransaction.toOutputDto()
    }
}