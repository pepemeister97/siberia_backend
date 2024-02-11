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
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.service.UserAccessControlService
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService

class StockService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()
    fun create(authorizedUser: AuthorizedUser, stockCreateDto: StockCreateDto): StockOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]

        val stockDao = StockDao.new {
            name = stockCreateDto.name
            address = stockCreateDto.address
        }
        val event = StockCreateEvent(userDao.login, stockCreateDto.name)
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
}