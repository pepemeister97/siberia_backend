package siberia.modules.auth.service

import io.ktor.util.date.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.ForbiddenException
import siberia.exceptions.UnauthorizedException
import siberia.modules.auth.data.dto.AuthInputDto
import siberia.modules.auth.data.dto.RefreshTokenDto
import siberia.modules.auth.data.dto.TokenOutputDto
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.models.UserModel
import siberia.modules.user.service.UserAccessControlService
import siberia.utils.kodein.KodeinService
import siberia.utils.security.bcrypt.CryptoUtil
import siberia.utils.security.jwt.JwtUtil

class AuthService(override val di: DI) : KodeinService(di) {
    private val userAccessControlService: UserAccessControlService by instance()

    private fun generateTokenPair(userDao: UserDao): TokenOutputDto {
        val accessToken = JwtUtil.createToken(userDao)
        val refreshToken = JwtUtil.createToken(userDao, refreshToken = true)

        return TokenOutputDto(accessToken, refreshToken)
    }

    fun refreshUser(refreshTokenDto: RefreshTokenDto): TokenOutputDto = transaction {
        try {
            val userDao = UserDao[refreshTokenDto.id]

            if (userDao.lastLogin != refreshTokenDto.lastLogin)
                throw ForbiddenException()
            userDao.lastLogin = getTimeMillis()
            userDao.flush()

            generateTokenPair(userDao)
        } catch (e: Exception) {
            throw ForbiddenException()
        }
    }

    fun auth(authInputDto: AuthInputDto): TokenOutputDto = transaction {
        val search = UserDao.find {
            UserModel.login eq authInputDto.login
        }
        val userDao = if (search.empty())
            throw UnauthorizedException()
        else
            search.first()

        if (!CryptoUtil.compare(authInputDto.password, userDao.hash))
            throw ForbiddenException()

        userDao.lastLogin = getTimeMillis()
        userDao.flush()
        generateTokenPair(userDao)
    }
}