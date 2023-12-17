package siberia.conf

data class RulesConf (
    val userManaging: Int,
    val rbacManaging: Int,
    val checkLogs: Int,
    val brandManaging: Int,
    val collectionManaging: Int,
    val categoryManaging: Int,
    val productsManaging: Int,

    val createIncomeRequest: Int,
    val approveIncomeRequest: Int,

    val createOutcomeRequest: Int,
    val approveOutcomeRequest: Int,

    val createTransferRequest: Int,
    val approveTransferRequestCreation: Int,
    val manageTransferRequest: Int,
    val approveTransferDelivery: Int,
    val solveNotDeliveredProblem: Int,
)