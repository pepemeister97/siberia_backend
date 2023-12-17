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
    private val requestTypeConf: ApplicationConfig = mainConfig.config("requestTypes")
    private val requestStatusConf: ApplicationConfig = mainConfig.config("requestStatuses")

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
        port = serverConfig.getInt("port"),
        fileLocation = serverConfig.getString("file-location")
    )

    val rules = RulesConf(
        userManaging = rulesConf.getInt("user-managing"),
        rbacManaging = rulesConf.getInt("rbac-managing"),
        checkLogs = rulesConf.getInt("check-logs"),
        brandManaging = rulesConf.getInt("brand-managing"),
        collectionManaging = rulesConf.getInt("collection-managing"),
        categoryManaging = rulesConf.getInt("category-manging"),
        productsManaging = rulesConf.getInt("products-managing"),

        createIncomeRequest = rulesConf.getInt("create-income-request"),
        approveIncomeRequest = rulesConf.getInt("approve-income-request"),

        createOutcomeRequest = rulesConf.getInt("create-outcome-request"),
        approveOutcomeRequest = rulesConf.getInt("approve-outcome-request"),

        createTransferRequest = rulesConf.getInt("create-transfer-request"),
        approveTransferRequestCreation = rulesConf.getInt("approve-transfer-request-creation"),
        manageTransferRequest = rulesConf.getInt("manage-transfer-request"),
        approveTransferDelivery = rulesConf.getInt("approve-transfer-delivery"),
        solveNotDeliveredProblem = rulesConf.getInt("solve-not-delivered-problem"),
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
        productEvent = objectTypesConf.getInt("product"),
        brandEvent = objectTypesConf.getInt("brand"),
        collectionEvent = objectTypesConf.getInt("collection"),
        categoryEvent = objectTypesConf.getInt("category"),
        transactionEvent = objectTypesConf.getInt("transaction")
    )

    val requestTypes = RequestTypeConf(
        income = requestTypeConf.getInt("income"),
        outcome = requestTypeConf.getInt("outcome"),
        transfer = requestTypeConf.getInt("transfer"),
    )

    val requestStatus = RequestStatusConf(
        open = requestStatusConf.getInt("open"),
        created = requestStatusConf.getInt("created"),
        creationCancelled = requestStatusConf.getInt("creation-cancelled"),
        inProgress = requestStatusConf.getInt("in-progress"),
        processingCancelled = requestStatusConf.getInt("processing-cancelled"),
        delivered = requestStatusConf.getInt("delivered"),
        notDelivered = requestStatusConf.getInt("not-delivered"),
        failed = requestStatusConf.getInt("failed"),
        processed = requestStatusConf.getInt("processed"),
    )
}