package siberia.modules.stock.service

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.auth.service.AuthSocketService
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
import siberia.modules.rbac.data.models.RbacModel
import siberia.plugins.Logger


class StockService(di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()
    private val authSocketService: AuthSocketService by instance()

    fun create(authorizedUser: AuthorizedUser, stockCreateDto: StockCreateDto, autoCommit: Boolean = true): StockOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]

        val stockDao = StockDao.new {
            name = stockCreateDto.name
            address = stockCreateDto.address
        }
        val event = StockCreateEvent(userDao.login, stockCreateDto.name, stockDao.idValue)
        SystemEventModel.logEvent(event)

        if (autoCommit)
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
        val relatedUser = RbacModel.getUsersRelatedToStock(stockId)
        stockDao.delete(userDao.login)

        commit()

        authSocketService.updateRules(relatedUser)

        StockRemoveResultDto(
            success = true,
            message = "Stock $stockName successfully removed"
        )
    }

    fun getAvailableByFilter(authorizedUser: AuthorizedUser, stockSearchDto: StockSearchDto): List<StockOutputDto> = transaction {
        StockModel
            .select {
                StockModel.id inList (userAccessControlService.getAvailableStocks(authorizedUser.id).map { it.key }) and
                createLikeCond(stockSearchDto.filters?.name, (StockModel.id neq 0), StockModel.name) and
                createLikeCond(stockSearchDto.filters?.address, (StockModel.id neq 0), StockModel.address)
            }
            .orderBy(StockModel.name to SortOrder.ASC)
            .let {
                if(stockSearchDto.pagination == null)
                    it
                else
                    it.limit(stockSearchDto.pagination.n, stockSearchDto.pagination.offset)
            }
            .map {
                StockOutputDto(
                    id = it[StockModel.id].value,
                    name = it[StockModel.name],
                    address = it[StockModel.address]
                )
            }
    }

    fun getAll(): List<StockOutputDto> = transaction {
        StockModel
            .selectAll()
            .orderBy(StockModel.name to SortOrder.ASC)
            .map {
                StockOutputDto(
                    id = it[StockModel.id].value,
                    name = it[StockModel.name],
                    address = it[StockModel.address]
                )
            }
    }

    fun getOne(authorizedUser: AuthorizedUser, stockId: Int): StockFullOutputDto = transaction {
        if (!(userAccessControlService.getAvailableStocks(authorizedUser.id).map { it.key }).contains(stockId))
            throw ForbiddenException()
        StockDao[stockId].fullOutput()
    }

    fun getStockForQr(authorizedUser: AuthorizedUser, stockId: Int): StockDao = transaction {
        val stockDao = StockDao[stockId]
        Logger.debug("REQUEST FROM USER", "main")
        Logger.debug(authorizedUser, "main")
        if (!userAccessControlService.checkAccessToStock(authorizedUser.id, stockId))
            throw ForbiddenException()

        stockDao
    }
}