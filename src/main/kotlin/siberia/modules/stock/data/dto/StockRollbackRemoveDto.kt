package siberia.modules.stock.data.dto

import kotlinx.serialization.Serializable
import siberia.modules.product.data.dto.ProductListItemOutputDto
import siberia.modules.rbac.data.dto.LinkedRuleInputDto

@Serializable
data class StockRollbackRemoveDto (
    val id: Int,
    val name: String,
    val address: String,
    val products: List<ProductListItemOutputDto>,
    // That fields holds rules relations between removed stock and users and roles
    val relatedUsers: Map<Int, List<Int>>,
    val relatedRoles: Map<Int, List<Int>>
) {
   val createDto: StockCreateDto get() = StockCreateDto(name, address)

    fun getRulesRelatedToRole(stockId: Int, roleId: Int) =
        (if (relatedRoles.containsKey(roleId))
            relatedRoles[roleId]!!
        else listOf()).map {
            LinkedRuleInputDto(ruleId = it, stockId = stockId)
        }

    fun getRulesRelatedToUser(stockId: Int, userId: Int) =
        (if (relatedUsers.containsKey(userId))
            relatedUsers[userId]!!
        else
            listOf()).map {
            LinkedRuleInputDto(ruleId = it, stockId = stockId)
        }

}