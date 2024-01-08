package siberia.utils.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
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
                database.url, driver = database.driver,
                user = database.user, password = database.password)
        }
    }
}