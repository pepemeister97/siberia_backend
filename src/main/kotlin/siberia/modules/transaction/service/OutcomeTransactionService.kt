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
import siberia.modules.transaction.data.dto.TransactionRemoveResultDto
import siberia.modules.transaction.data.models.TransactionModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.idValue

class OutcomeTransactionService(di: DI) : AbstractTransactionService(di) {
    fun create(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto): TransactionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        if (transactionInputDto.type != AppConf.requestTypes.outcome)
            throw BadRequestException("Bad transaction type")
        val targetStockId = transactionInputDto.from ?: throw BadRequestException("Incorrect target stock")

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
        if (transactionDao.typeId != AppConf.requestTypes.outcome)
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

    fun updateHidden(transactionId: Int, products: List<TransactionInputDto.TransactionProductInputDto>) = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.outcome || transactionDao.statusId != AppConf.requestStatus.open)
            throw ForbiddenException()
        val targetStockId = transactionDao.fromId ?: throw BadRequestException("Bad transaction")
        val oldList = TransactionModel.clearProductsList(transactionId)
        StockModel.appendProducts(targetStockId, oldList.map { TransactionInputDto.TransactionProductInputDto(
            productId = it.product.id,
            amount = it.amount,
            price = it.price
        ) })

        TransactionModel.addProductList(transactionId, products)
        StockModel.removeProducts(targetStockId, products)

        transactionDao.toOutputDto()
    }

    fun removeHidden(transactionId: Int) = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (!transactionDao.hidden || transactionDao.typeId != AppConf.requestTypes.outcome)
            throw ForbiddenException()
        val targetStockId = transactionDao.fromId ?: throw BadRequestException("Bad transaction")
        StockModel.appendProducts(targetStockId, transactionDao.inputProductsList)
        transactionDao.delete()

        TransactionRemoveResultDto(true, "Removed successfully")
    }

    fun createFromHidden(authorizedUser: AuthorizedUser, transactionId: Int) = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (
            !transactionDao.hidden ||
            transactionDao.typeId != AppConf.requestTypes.outcome ||
            !userAccessControlService.checkAccessToStock(authorizedUser.id, AppConf.rules.createOutcomeRequest, transactionDao.fromId!!)
        )
            throw ForbiddenException()

        transactionDao.hidden = false
        transactionDao.flush()

        transactionDao.toOutputDto()
    }
}