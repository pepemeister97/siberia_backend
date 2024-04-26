package siberia.modules.auth.service

import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
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
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.user.data.dao.UserDao
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
    private val authQrService: AuthQrService by instance()

    private data class RefreshEvent(
        val userId: Int,
        val newLastLogin: Long
    )

    @OptIn(ObsoleteCoroutinesApi::class)
    private val userRefreshChannel = CoroutineScope(Job()).actor<RefreshEvent>(capacity = Channel.BUFFERED) {
        for (event in this) {
            updateUserLastLogin(event.userId, event.newLastLogin)
        }
    }

    private fun generateTokenPair(userId: Int, refreshTime: Long): TokenOutputDto {
        val accessToken = JwtUtil.createToken(userId)
        val refreshToken = JwtUtil.createToken(userId, lastLogin = refreshTime)

        return TokenOutputDto(accessToken, refreshToken)
    }

    private fun updateUserLastLogin(userId: Int, lastLogin: Long) = transaction {
        UserLoginModel.deleteWhere {
            UserLoginModel.userId eq userId
        }

        UserLoginModel.insert {
            it[UserLoginModel.userId] = userId
            it[UserLoginModel.lastLogin] = lastLogin
        }

        commit()
    }

    fun refreshUser(refreshTokenDto: RefreshTokenDto): TokenOutputDto {
        return try {
            val user = transaction {
                val selected = UserModel.select { UserModel.id eq refreshTokenDto.id }

                if (selected.count() == 0L)
                    throw ForbiddenException()

                selected.first()[UserModel.id].value
            }
            val newLastLogin = getTimeMillis()
            userRefreshChannel.trySend(RefreshEvent(user, newLastLogin))
            val tokenPair = generateTokenPair(user, newLastLogin)
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
        val stockData = StockDao[authorizedUser.stockId].toOutputDto()
        val operationAccessData = MobileOperationAccessDto(
            arrivalsManaging = userAccessControlService.checkAccessToStock(authorizedUser.id, AppConf.rules.createIncomeRequest, targetStockId),
            salesManaging = userAccessControlService.checkAccessToStock(authorizedUser.id, AppConf.rules.createOutcomeRequest, targetStockId),
            transfersManaging = userAccessControlService.checkAccessToStock(authorizedUser.id, AppConf.rules.createTransferRequest, targetStockId),
            transfersProcessing = UserDao[authorizedUser.id].hasAccessToProcessTransfers
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
        ).apply {
            Logger.debug(this, "main")
        }
    }
}