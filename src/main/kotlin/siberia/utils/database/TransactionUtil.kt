package siberia.utils.database

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransaction
import java.sql.Connection.TRANSACTION_READ_COMMITTED
import java.sql.SQLNonTransientConnectionException

suspend fun <T> suspendedTransaction(statements: suspend Transaction.() -> T) = TransactionManager.currentOrNew(TRANSACTION_READ_COMMITTED).suspendedTransaction {
    try {
        if (connection.isClosed)
            DatabaseConnector.connect()
        statements()
    } catch (e: Exception) {
        throw e
    } catch (e: ExposedSQLException) {
        if (e.cause is SQLNonTransientConnectionException){
            DatabaseConnector.connect()
            rollback()
            statements()
        } else {
            throw e
        }
    }
}

fun <T> transaction(statements: Transaction.() -> T) = TransactionManager.currentOrNew(TRANSACTION_READ_COMMITTED).run {
    try {
        if (connection.isClosed)
            DatabaseConnector.connect()
        statements()
    } catch (e: Exception) {
        throw e
    } catch (e: ExposedSQLException) {
        if (e.cause is SQLNonTransientConnectionException){
            DatabaseConnector.connect()
            rollback()
            statements()
        } else {
            throw e
        }
    }
}