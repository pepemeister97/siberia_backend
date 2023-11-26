package siberia.modules.auth.service

import io.ktor.util.date.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dao.RoleDao
import siberia.modules.auth.data.dao.RuleDao
import siberia.modules.auth.data.dao.UserToRuleDao
import siberia.modules.auth.data.dto.authorization.AuthInputDto
import siberia.modules.auth.data.dto.authorization.CreateUserInputDto
import siberia.modules.auth.data.dto.authorization.RefreshTokenDto
import siberia.modules.auth.data.dto.authorization.TokenOutputDto
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.models.UserModel
import siberia.utils.kodein.KodeinService
import siberia.utils.security.bcrypt.CryptoUtil
import siberia.utils.security.jwt.JwtUtil

class AuthService(override val di: DI) : KodeinService(di) {

    private fun generateTokenPair(userDao: UserDao): TokenOutputDto {
        val accessToken = JwtUtil.createToken(userDao)
        val refreshToken = JwtUtil.createToken(userDao, refreshToken = true)

        return TokenOutputDto(accessToken, refreshToken)
    }

    fun refreshUser(refreshTokenDto: RefreshTokenDto): TokenOutputDto = transaction {
        try {
            val userDao = UserDao[refreshTokenDto.id]

            if (userDao.lastLogin != refreshTokenDto.lastLogin)
                throw Exception("forbidden")
            userDao.lastLogin = getTimeMillis()
            userDao.flush()

            generateTokenPair(userDao)
        } catch (e: Exception) {
            throw e
        }
    }

    fun auth(authInputDto: AuthInputDto): TokenOutputDto = transaction {
        try {
            val search = UserDao.find {
                UserModel.login eq authInputDto.login
            }
            val userDao = if (search.empty())
                throw Exception("unauthorized")
            else
                search.first()

            if (!CryptoUtil.compare(authInputDto.password, userDao.hash))
                throw Exception("forbidden")

            userDao.lastLogin = getTimeMillis()
            userDao.flush()
            generateTokenPair(userDao)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun addRuleToUser(userDao: UserDao, ruleId: Int, stockId: Int? = null) {
        try {
            val ruleDao = RuleDao[ruleId]
            UserToRuleDao.new {
                user = userDao
                rule = ruleDao
                if (rule.needStock) {
                    if (stockId != null)
                        stock = StockDao[stockId]
                    else
                        throw Exception("must provide stock id")
                }
            }
        } catch (e: Exception) {
            throw Exception("Bad rule: ${e.message}")
        }
    }

    private fun addRoleToUser(userDao: UserDao, roleId: Int) {
        try {
            val role = RoleDao[roleId]
            role.outputWithChildren.rules.forEach {
                addRuleToUser(userDao, it.ruleId, it.stockId)
            }
        } catch (e: Exception) {
            throw Exception("Bad role")
        }
    }

    fun signUp(createUserInputDto: CreateUserInputDto): TokenOutputDto = transaction {
        val search = UserDao.find {
            UserModel.login eq createUserInputDto.login
        }
        if (!search.empty()) {
            throw Exception("bad login")
        }

        val userDao = UserDao.new {
            login = createUserInputDto.login
            hash = CryptoUtil.hash(createUserInputDto.password)
            lastLogin = getTimeMillis()
        }

        createUserInputDto.rules.forEach {
            try {
                addRuleToUser(userDao, it.ruleId, it.stockId)
            } catch (e: Exception) {
                rollback()
                throw e
            }
        }

        createUserInputDto.roles.forEach {
            try {
                addRoleToUser(userDao, it)
            } catch (e: Exception) {
                rollback()
                throw e
            }
        }

        commit()

        generateTokenPair(userDao)
    }
}