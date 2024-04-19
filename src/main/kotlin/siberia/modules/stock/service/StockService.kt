package siberia.modules.stock.service

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.rbac.data.dto.LinkedRuleInputDto
import siberia.modules.stock.data.dto.StockFullOutputDto
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.stock.data.dao.StockDao.Companion.createLikeCond
import siberia.modules.stock.data.dto.*
import siberia.modules.stock.data.dto.systemevents.StockCreateEvent
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.service.UserAccessControlService
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.exposed.sql.select
import siberia.modules.product.service.ProductService
import siberia.conf.AppConf.requestTypes
import siberia.modules.transaction.data.dto.TransactionInputDto.TransactionProductInputDto
import siberia.modules.stock.data.models.StockToProductModel
import siberia.plugins.Logger

class StockService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()
    private val productService: ProductService by instance()
    fun create(authorizedUser: AuthorizedUser, stockCreateDto: StockCreateDto): StockOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]

        val stockDao = StockDao.new {
            name = stockCreateDto.name
            address = stockCreateDto.address
        }
        val event = StockCreateEvent(userDao.login, stockCreateDto.name, stockDao.idValue)
        SystemEventModel.logEvent(event)
        commit()

        userAccessControlService.addRules(authorizedUser, userDao.idValue, listOf(LinkedRuleInputDto(AppConf.rules.concreteStockView, stockDao.idValue)))

        stockDao.toOutputDto()
    }

    fun update(authorizedUser: AuthorizedUser, stockId: Int, stockUpdateDto: StockUpdateDto): StockOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val stockDao = StockDao[stockId]
        stockDao.loadAndFlush(userDao.login, stockUpdateDto)

        commit()

        stockDao.toOutputDto()
    }

    fun remove(authorizedUser: AuthorizedUser, stockId: Int): StockRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val stockDao = StockDao[stockId]
        val stockName = stockDao.name
        stockDao.delete(userDao.login)

        commit()

        StockRemoveResultDto(
            success = true,
            message = "Stock $stockName successfully removed"
        )
    }

//    fun getByFilter(stockSearchDto: StockSearchDto): List<StockOutputDto> = transaction {
//        StockDao.find {
//            createLikeCond(stockSearchDto.filters?.name, (StockModel.id neq 0), StockModel.name) and
//            createLikeCond(stockSearchDto.filters?.address, (StockModel.id neq 0), StockModel.address)
//        }.let {
//            if (stockSearchDto.pagination == null)
//                it
//            else
//                it.limit(stockSearchDto.pagination.n, stockSearchDto.pagination.offset)
//        }.map { it.toOutputDto() }
//    }

    fun getAvailableByFilter(authorizedUser: AuthorizedUser, stockSearchDto: StockSearchDto): List<StockOutputDto> = transaction {
        StockDao.find {
            StockModel.id inList (userAccessControlService.getAvailableStocks(authorizedUser.id).map { it.key }) and
            createLikeCond(stockSearchDto.filters?.name, (StockModel.id neq 0), StockModel.name) and
            createLikeCond(stockSearchDto.filters?.address, (StockModel.id neq 0), StockModel.address)
        }.let {
            if(stockSearchDto.pagination == null)
                it
            else
                it.limit(stockSearchDto.pagination.n, stockSearchDto.pagination.offset)
        }.map { it.toOutputDto() }
    }

    fun getAll(): List<StockOutputDto> = transaction {
        StockDao.all().map { it.toOutputDto() }
    }

    fun getOne(authorizedUser: AuthorizedUser, stockId: Int): StockFullOutputDto = transaction {
        if (!(userAccessControlService.getAvailableStocks(authorizedUser.id).map { it.key }).contains(stockId))
            throw ForbiddenException()
        StockDao[stockId].fullOutput()
    }

    //For mobile apps where token contains stock id
    fun getByAuthorizedUser(authorizedUser: AuthorizedUser) = transaction {
        if (userAccessControlService.getAvailableStocks(authorizedUser.id).filter { it.key == authorizedUser.stockId }.isEmpty())
            throw ForbiddenException()
        StockDao[authorizedUser.stockId ?: 0].toOutputDto()
    }

    fun getStockForQr(authorizedUser: AuthorizedUser, stockId: Int): StockDao = transaction {
        val stockDao = StockDao[stockId]

        if (!userAccessControlService.checkAccessToStock(authorizedUser.id, stockId))
            throw ForbiddenException()

        stockDao
    }

    fun checkProductAvailability(stockId: Int, authorizedUser: AuthorizedUser): Boolean = transaction {
        val userStockAccess = userAccessControlService.getAvailableStocks(authorizedUser.id).map { it.key }
        var output = true
        if (!userStockAccess.contains(stockId)) {
            Logger.debug("User does not have access to stock $stockId.", "main")
            output = false
        }
        output
    }

    fun getProductStockInfo(productId: Int): TransactionProductInputDto = transaction {
        val productStock = StockToProductModel
            .slice(StockToProductModel.amount, StockToProductModel.price)
            .select { StockToProductModel.product eq productId }
            .map { row ->
                TransactionProductInputDto(
                    productId = productId,
                    amount = row[StockToProductModel.amount],
                    price = row[StockToProductModel.price]
                )
            }.singleOrNull() ?: TransactionProductInputDto(productId, 0.0, null)
        productStock
    }

    fun generateSale(authorizedUser: AuthorizedUser, workbook: XSSFWorkbook,  stockId: Int):TransactionInputDto  {
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
            Logger.debug("Some required columns are missing in the header.", "main")
            //throw IllegalArgumentException("Some required columns are missing in the header.")
        }


        val productListFromXls = mutableListOf<TransactionProductInputDto>()
        for (row in sheet.rowIterator()) {
            if (row.rowNum == 0) continue
            val id = row.getCell(idIndex).numericCellValue.toInt()
            val amount = row.getCell(amountIndex).numericCellValue.toDouble()
            val price = row.getCell(priceIndex).numericCellValue.toDouble()
            productListFromXls.add(TransactionProductInputDto(productId = id, amount = amount, price = price)) // then we take products form xls
        }

        workbook.close()

        val productListToSell = mutableListOf<TransactionProductInputDto>()
        if (checkProductAvailability(stockId, authorizedUser)){      // end check if user authorized and products exists
            for (productXls in productListFromXls) {
                val productStock = getProductStockInfo(productXls.productId)
                if (productXls.amount <= productStock.amount) {       // then if product amount in stock is enough
                    productListToSell.add(productXls)           // we take amount from exel
                } else {
                    productListToSell.add(productStock)         // else, product amount in the stock - is all we can sell
                }
            }
        } else {
            Logger.debug("! User is unauthorized or products in the Excel file do not exist !","main")
        }

        return TransactionInputDto(
            from = stockId,
            to = null,
            type = requestTypes.outcome,
            products = productListToSell,
            hidden = false
        )
    }
}