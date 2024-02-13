package siberia.modules.auth.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import qrcode.QRCode
import qrcode.color.Colors
import siberia.exceptions.ForbiddenException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.auth.data.dto.QrTokenDto
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.user.data.dao.UserDao
import siberia.plugins.Logger
import siberia.utils.database.idValue
import siberia.utils.kodein.KodeinService
import siberia.utils.security.jwt.JwtUtil

class AuthQrService(di: DI) : KodeinService(di) {
    private fun createQr(data: String) =
        QRCode
            .ofRoundedSquares()
            .withColor(Colors.BLACK)
            .withSize(5)
            .build(data)
            .renderToBytes()
    fun createStockQr(authorizedUser: AuthorizedUser, stockId: Int): ByteArray = transaction {
        try {
            val userDao = UserDao[authorizedUser.id]
            val stockDao = StockDao[stockId]
            val token = JwtUtil.createMobileAccessToken(QrTokenDto(userDao.idValue, stockId = stockDao.idValue))
            createQr(token)
        } catch (e: Exception) {
            Logger.debugException("Exception during QR generation", e, "main")
            throw ForbiddenException()
        }
    }

    fun createTransactionQr(authorizedUser: AuthorizedUser, transactionId: Int): ByteArray = transaction {
        try {
            val userDao = UserDao[authorizedUser.id]
            val transactionDao = TransactionDao[transactionId]
            val token = JwtUtil.createMobileAccessToken(QrTokenDto(userDao.idValue, transactionId = transactionDao.idValue))
            createQr(token)
        } catch (e: Exception) {
            Logger.debugException("Exception during QR generation", e, "main")
            throw ForbiddenException()
        }
    }
}