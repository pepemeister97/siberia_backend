package siberia.modules.auth.service

import io.ktor.util.date.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.conf.AppConf
import siberia.exceptions.ForbiddenException
import siberia.exceptions.UnauthorizedException
import siberia.modules.auth.data.dto.*
import siberia.modules.auth.data.models.UserLoginModel
import siberia.modules.rbac.data.models.RbacModel
import siberia.modules.stock.service.StockService
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.user.data.dto.UserOutputDto
import siberia.modules.user.data.models.UserModel
import siberia.modules.user.service.UserAccessControlService
import siberia.modules.user.service.UserService
import siberia.plugins.Logger
import siberia.utils.kodein.KodeinService
import siberia.utils.security.bcrypt.CryptoUtil
import siberia.utils.security.jwt.JwtUtil

class AuthService(override val di: DI) : KodeinService(di) {
    private val userService: UserService by instance()
    private val userAccessControlService: UserAccessControlService by instance()
    private val stockService: StockService by instance()
    private val authQrService: AuthQrService by instance()
    private fun generateTokenPair(userId: Int, refreshTime: Long): TokenOutputDto {
        val accessToken = JwtUtil.createToken(userId)
        val refreshToken = JwtUtil.createToken(userId, lastLogin = refreshTime)

        return TokenOutputDto(accessToken, refreshToken)
    }

    private fun updateUserLastLogin(userId: Int, lastLogin: Long) {
        UserLoginModel.deleteWhere {
            UserLoginModel.userId eq userId
        }

        UserLoginModel.insert {
            it[UserLoginModel.userId] = userId
            it[UserLoginModel.lastLogin] = lastLogin
        }
    }

    fun refreshUser(refreshTokenDto: RefreshTokenDto): TokenOutputDto = transaction {
        try {
            val selected = UserModel.select { UserModel.id eq refreshTokenDto.id }
            if (selected.count() == 0L)
                throw ForbiddenException()
            val user = selected.first()[UserModel.id].value
            val newLastLogin = getTimeMillis()
            updateUserLastLogin(user, newLastLogin)
            val tokenPair = generateTokenPair(user, newLastLogin)
            commit()
            tokenPair
        } catch (e: Exception) {
            Logger.debugException("Exception during refresh", e, "main")
            throw ForbiddenException()
        }
    }

    fun auth(authInputDto: AuthInputDto): TokenOutputDto = transaction {
        val search = UserModel.select {
            UserModel.login eq authInputDto.login
        }
        val user = if (search.empty())
            throw UnauthorizedException()
        else
            search.first()

        if (!CryptoUtil.compare(authInputDto.password, user[UserModel.hash]))
            throw ForbiddenException()

        val userId = user[UserModel.id].value
        val lastLogin = getTimeMillis()
        updateUserLastLogin(userId, lastLogin)

        val tokenPair = generateTokenPair(userId, lastLogin)
        commit()
        tokenPair
    }

    fun getAuthorized(authorizedUser: AuthorizedUser): UserOutputDto {
        val userDto = userService.getOne(authorizedUser.id)
        userDto.rules = RbacModel.userToRuleLinks(
            userDto.id, expanded = true
        )
        return userDto
    }

    fun getAuthenticatedStockData(authorizedUser: AuthorizedUser): AuthenticatedStockOutputDto = transaction {
        val targetStockId = authorizedUser.stockId ?: throw ForbiddenException()
        val stockData = stockService.getByAuthorizedUser(authorizedUser)
        val operationAccessData = MobileOperationAccessDto(
            arrivalsManaging = userAccessControlService.checkAccessToStock(authorizedUser.id, AppConf.rules.createIncomeRequest, targetStockId),
            salesManaging = userAccessControlService.checkAccessToStock(authorizedUser.id, AppConf.rules.createOutcomeRequest, targetStockId),
            transfersManaging = userAccessControlService.checkAccessToStock(authorizedUser.id, AppConf.rules.createTransferRequest, targetStockId),
        )


        AuthenticatedStockOutputDto(
            stockData = stockData,
            operationsAccess = operationAccessData,
            type = authQrService.getMobileTokenType(authorizedUser),
            transactionData = with(authorizedUser.transactionId) {
                if (this != null)
                    TransactionDao[this].fullOutput()
                else
                    null
            }
        )
    }
}