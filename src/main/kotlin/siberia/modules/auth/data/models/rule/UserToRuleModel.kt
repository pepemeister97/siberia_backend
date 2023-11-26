package siberia.modules.auth.data.models.rule

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.stock.data.models.StockModel
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntIdTable

object UserToRuleModel: BaseIntIdTable() {
    val user = reference("user", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val rule = reference("rule", RuleModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val stock = reference("stock", StockModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
}