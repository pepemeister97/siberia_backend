package siberia.conf

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

object AppConf {
    private val mainConfig: ApplicationConfig = HoconApplicationConfig(ConfigFactory.load().getConfig("application"))
    private val jwtConfig: ApplicationConfig = mainConfig.config("jwt")
    private val databaseConfig: ApplicationConfig = mainConfig.config("database")
    private val serverConfig: ApplicationConfig = mainConfig.config("server")

    private fun ApplicationConfig.getString(name: String): String = this.property(name).getString()
    private fun ApplicationConfig.getInt(name: String): Int = this.getString(name).toInt()

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
}