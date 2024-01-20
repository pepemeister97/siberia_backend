package siberia.utils.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import siberia.conf.AppConf.database

class DatabaseConnector(vararg tables: Table, initializer: Transaction.() -> Unit) {
    init {
        connect()
        transaction {
            tables.forEach {
                SchemaUtils.create(it)
            }
            initializer()
        }
    }
    companion object {
        fun connect() {
            TransactionManager.defaultDatabase = Database.connect(
                HikariDataSource(
                    HikariConfig().apply {
                        driverClassName = database.driver
                        jdbcUrl = database.url
                        username = database.user
                        password = database.password
                        maximumPoolSize = 10
                        isAutoCommit = false
                        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                        validate()
                    }
                ),
                databaseConfig = DatabaseConfig.invoke { maxEntitiesToStoreInCachePerEntity = 0 }
            )
        }
    }
}