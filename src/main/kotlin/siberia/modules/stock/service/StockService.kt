package siberia.modules.stock.service

import org.jetbrains.exposed.sql.and
import siberia.utils.database.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.stock.data.StockFullOutputDto
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.stock.data.dao.StockDao.Companion.createLikeCond
import siberia.modules.stock.data.dto.*
import siberia.modules.stock.data.dto.systemevents.StockCreateEvent
import siberia.modules.stock.data.dto.systemevents.StockRemoveEvent
import siberia.modules.stock.data.dto.systemevents.StockUpdateEvent
import siberia.modules.stock.data.models.StockModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.kodein.KodeinService

class StockService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, stockCreateDto: StockCreateDto): StockOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]

        val stockDao = StockDao.new {
            name = stockCreateDto.name
            address = stockCreateDto.address
        }
        val event = StockCreateEvent(userDao.login, stockCreateDto.name)
        SystemEventModel.logEvent(event)
        commit()

        stockDao.toOutputDto()
    }

    fun update(authorizedUser: AuthorizedUser, stockId: Int, stockUpdateDto: StockUpdateDto): StockOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val stockDao = StockDao[stockId]

        stockDao.loadUpdateDto(stockUpdateDto)
        stockDao.flush()
        val event = StockUpdateEvent(userDao.login, stockDao.name)
        SystemEventModel.logEvent(event)
        commit()

        stockDao.toOutputDto()
    }

    fun remove(authorizedUser: AuthorizedUser, stockId: Int): StockRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val stockDao = StockDao[stockId]
        val stockName = stockDao.name

        val event = StockRemoveEvent(userDao.login, stockName)
        SystemEventModel.logEvent(event)
        commit()

        StockRemoveResultDto(
            success = true,
            message = "Stock $stockName successfully removed"
        )
    }

    fun getByFilter(stockSearchDto: StockSearchDto): List<StockOutputDto> = transaction {
        StockDao.find {
            createLikeCond(stockSearchDto.filters.name, (StockModel.id neq 0), StockModel.name) and
            createLikeCond(stockSearchDto.filters.address, (StockModel.id neq 0), StockModel.address)
        }.limit(stockSearchDto.pagination.n, stockSearchDto.pagination.offset).map { it.toOutputDto() }
    }

    fun getOne(stockId: Int): StockFullOutputDto =
        StockDao[stockId].fullOutput()
}