package siberia.modules.transaction.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.product.service.ProductService
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.data.dto.TransactionOutputDto
import siberia.modules.transaction.data.models.TransactionModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.database.idValue

class IncomeTransactionService(di: DI) : AbstractTransactionService(di) {
    private val productService: ProductService by instance()
    fun create(authorizedUser: AuthorizedUser, transactionInputDto: TransactionInputDto): TransactionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val targetStockId = transactionInputDto.to ?: throw BadRequestException("Incorrect target stock")
        val transactionDao = createTransaction(userDao, transactionInputDto, targetStockId)

        commit()

        try {
            processed(authorizedUser, transactionDao.idValue)
        } catch (e: ForbiddenException) {
            transactionDao.toOutputDto()
        }
    }

    fun cancelCreation(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.income)
            throw ForbiddenException()
        val targetStockId = transactionDao.toId ?: throw BadRequestException("Bad transaction")

        changeStatusTo(
            authorizedUser,
            transactionId,
            targetStockId,
            AppConf.requestStatus.creationCancelled
        ).toOutputDto()
    }

    fun processed(authorizedUser: AuthorizedUser, transactionId: Int): TransactionOutputDto = transaction {
        val transactionDao = TransactionDao[transactionId]
        if (transactionDao.typeId != AppConf.requestTypes.income)
            throw ForbiddenException()

        val targetStockId = transactionDao.toId ?: throw BadRequestException("Bad transaction")
        val approvedTransaction = changeStatusTo(authorizedUser, transactionId, targetStockId, AppConf.requestStatus.processed)
        StockDao[targetStockId]
        val products = TransactionModel.getFullProductList(transactionId)
        productService.updateLastPurchaseData(products, approvedTransaction.updatedAt ?: approvedTransaction.createdAt)
        StockModel.appendProducts(targetStockId, products.map {
            TransactionInputDto.TransactionProductInputDto(
                it.product.id,
                it.amount
            )
        })

        approvedTransaction.toOutputDto()
    }
}