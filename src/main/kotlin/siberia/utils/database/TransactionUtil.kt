package siberia.utils.database

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection.TRANSACTION_READ_COMMITTED

fun <T> transaction(statements: Transaction.() -> T) = TransactionManager.currentOrNew(TRANSACTION_READ_COMMITTED).run {
    try {
        statements()
    } catch (e: Exception) {
        throw e
    } catch (e: ExposedSQLException) {
        throw e
    }
}