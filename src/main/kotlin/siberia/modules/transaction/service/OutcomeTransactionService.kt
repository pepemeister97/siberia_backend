package siberia.modules.transaction.service
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.stock.data.models.StockModel
import siberia.modules.stock.service.StockService
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.data.dto.TransactionOutputDto
import siberia.modules.transaction.data.dto.TransactionRemoveResultDto
import siberia.modules.transaction.data.models.TransactionModel
import siberia.modules.user.data.dao.UserDao
import siberia.plugins.Logger
import siberia.utils.database.idValue

class OutcomeTransactionService(di: DI) : AbstractTransactionService(di) {
    private val stockService: StockService by instance()
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

    fun create(authorizedUser: AuthorizedUser, workbook: XSSFWorkbook, stockId: Int):TransactionInputDto  {
        val sheet = workbook.getSheetAt(0)

        val headerRow1 = sheet.getRow(0)
        var idIndex = -1
        var amountIndex = -1
        var priceIndex = -1
        Logger.debug(headerRow1, "main")

        for (cell in headerRow1.cellIterator()) {     // try to find required columns
            when (cell.stringCellValue.lowercase()) {
                "id" -> idIndex = cell.columnIndex
                "amount" -> amountIndex = cell.columnIndex
                "price" -> priceIndex = cell.columnIndex
            }
        }
        if (idIndex == -1 || amountIndex == -1 || priceIndex == -1) {     // continue only if we got all required fields in header
            workbook.close()
            throw BadRequestException("Some required columns are missing in the header.")
        }

        val productListToSell = mutableListOf<TransactionInputDto.TransactionProductInputDto>()
        val productList = stockService.getOne(authorizedUser, stockId).products

        for (row in sheet.rowIterator()) {
            if (row.rowNum == 0) continue
            val productId = row.getCell(idIndex).numericCellValue.toInt()
            val productAmount = row.getCell(amountIndex).numericCellValue.toDouble()

            val product = productList.find { it.id == productId }
            if (product!=null){
                if (product.quantity <= productAmount) {       // then if product amount in stock is enough
                    productListToSell.add(
                        TransactionInputDto.TransactionProductInputDto(
                            productId = product.id,
                            amount = product.quantity,
                            price = product.price
                        )
                    )

                } else {
                    throw BadRequestException("Not enough products in stock !")
                }
            } else {
                throw BadRequestException("One of the requested products is out of stock !")
            }
        }
        workbook.close()

        return TransactionInputDto(
            from = stockId,
            to = null,
            type = AppConf.requestTypes.outcome,
            products = productListToSell,
            hidden = false
        )
    }
}