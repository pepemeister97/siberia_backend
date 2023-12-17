package siberia.utils.database

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransaction
import java.sql.Connection.TRANSACTION_READ_COMMITTED

suspend fun <T> suspendedTransaction(statements: suspend Transaction.() -> T) = TransactionManager.currentOrNew(TRANSACTION_READ_COMMITTED).suspendedTransaction {
    try {
        statements()
    } catch (e: Exception) {
        throw e
    } catch (e: ExposedSQLException) {
        throw e
    }
}

fun <T> transaction(statements: Transaction.() -> T) = TransactionManager.currentOrNew(TRANSACTION_READ_COMMITTED).run {
    try {
        statements()
    } catch (e: Exception) {
        throw e
    } catch (e: ExposedSQLException) {
        throw e
    }
}


interface TransactionalService {
    suspend fun <T> transaction(statements: suspend Transaction.() -> T) = suspendedTransaction(statements)

}