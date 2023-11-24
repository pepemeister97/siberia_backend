package siberia.utils.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.conf.AppConf.database

class DatabaseConnector(vararg tables: Table, initializer: Transaction.() -> Unit) {
    init {
        TransactionManager.defaultDatabase = Database.connect(
            database.url, driver = database.driver,
            user = database.user, password = database.password)

        transaction {
            tables.forEach {
                SchemaUtils.create(it)
            }
            initializer()
        }
    }
}