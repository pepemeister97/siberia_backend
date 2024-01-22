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
        private lateinit var hikariDataSource: HikariDataSource
        fun connect() {
            hikariDataSource = HikariDataSource(
                HikariConfig().apply {
                    driverClassName = database.driver
                    jdbcUrl = database.url
                    username = database.user
                    password = database.password
                    maximumPoolSize = 5
                    maxLifetime = 600000
                    keepaliveTime = 300000
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                    validate()
                }
            )
            TransactionManager.defaultDatabase = Database.connect(
                hikariDataSource,
                databaseConfig = DatabaseConfig.invoke { maxEntitiesToStoreInCachePerEntity = 0 }
            )
        }
    }
}