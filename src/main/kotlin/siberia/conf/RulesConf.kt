package siberia.conf

data class RulesConf (
    val userManaging: Int,
    val rbacManaging: Int,
    val checkLogs: Int,
    val brandManaging: Int,
    val collectionManaging: Int,
    val categoryManaging: Int,
    val productsManaging: Int,
    val viewProductsList: Int,
    val stockManaging: Int,

    val concreteStockView: Int,

    val createWriteOffRequest: Int,

    val createIncomeRequest: Int,

    val createOutcomeRequest: Int,

    val createTransferRequest: Int,
    val manageTransferRequest: Int,
    val solveNotDeliveredProblem: Int,

    //Specific rules for mobile app authorization
    //ONLY for token verifying
    val mobileAuth: Int,
    val mobileAccess: Int
)