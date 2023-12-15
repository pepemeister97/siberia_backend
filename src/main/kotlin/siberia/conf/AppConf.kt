package siberia.conf

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

object AppConf {
    private val mainConfig: ApplicationConfig = HoconApplicationConfig(ConfigFactory.load().getConfig("application"))
    private val jwtConfig: ApplicationConfig = mainConfig.config("jwt")
    private val databaseConfig: ApplicationConfig = mainConfig.config("database")
    private val serverConfig: ApplicationConfig = mainConfig.config("server")
    private val rulesConf: ApplicationConfig = mainConfig.config("rules")
    private val eventTypesConf: ApplicationConfig = mainConfig.config("eventTypes")
    private val objectTypesConf: ApplicationConfig = mainConfig.config("objectTypes")

    private fun ApplicationConfig.getString(name: String): String = this.property(name).getString()
    private fun ApplicationConfig.getInt(name: String): Int = this.getString(name).toInt()

    val isDebug: Boolean = mainConfig.getString("debug") == "true"

    val jwt = JwtConf(
        domain = jwtConfig.getString("domain"),
        secret = jwtConfig.getString("secret"),
        expirationTime = jwtConfig.config("expiration").getInt("seconds"),
        refreshExpirationTime = jwtConfig.config("refreshExpiration").getInt("seconds")
    )

    val database = DatabaseConf(
        url = databaseConfig.getString("url"),
        driver = databaseConfig.getString("driver"),
        user = databaseConfig.getString("user"),
        password = databaseConfig.getString("password")
    )

    val server = ServerConf(
        host = serverConfig.getString("host"),
        port = serverConfig.getInt("port")
    )

    val rules = RulesConf(
        userManaging = rulesConf.getInt("user-managing"),
        rbacManaging = rulesConf.getInt("rbac-managing"),
        checkLogs = rulesConf.getInt("check-logs")
    )

    val eventTypes = EventTypesConf(
        createEvent = eventTypesConf.getInt("create"),
        updateEvent = eventTypesConf.getInt("update"),
        removeEvent = eventTypesConf.getInt("remove")
    )

    val objectTypes = ObjectTypesConf(
        userEvent = objectTypesConf.getInt("user"),
        stockEvent = objectTypesConf.getInt("stock"),
        roleEvent = objectTypesConf.getInt("role"),
        productEvent = objectTypesConf.getInt("product")
    )
}