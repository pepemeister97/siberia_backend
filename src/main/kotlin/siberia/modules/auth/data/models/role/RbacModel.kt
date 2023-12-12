package siberia.modules.auth.data.models.role

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.modules.auth.data.dto.LinkedRuleInputDto
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.auth.data.dto.RoleOutputDto
import siberia.modules.auth.data.models.rule.RuleModel
import siberia.modules.stock.data.models.StockModel
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntIdTable

object RbacModel: BaseIntIdTable() {
    val user = reference("user", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
    val role = reference("user", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
    val rule = reference("rule", RuleModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
    val stock = reference("stock", StockModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)

    // it links on "main" row which is simplified this
    // field means that current row is created to expand some relations:
    // 1) user-role relation to be the many user-rule relations
    // 2) user-rule-stock and role-rule-stock (forAll) to be the manu user-rule-stocks and role-rule-stocks relations
    val simplifiedBy = reference("simplified_by", RbacModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)

    // Field is created for user-rule-stock and role-rule-stock (rules with stock) relations
    // it means that user have access to all stocks by this rule ex. can view all stocks
    // For such rule will be automatically generated all user-rule-stocks relations and marked as simplifiedBy this one
    val forAll = bool("for_all").default(false)

    fun getRuleLinks(query: Op<Boolean>, withStock: Boolean, expanded: Boolean = false): List<LinkedRuleOutputDto> = transaction {
        val cols = mutableListOf<Expression<*>>(rule, stock, RuleModel.category, RuleModel.needStock)
        RbacModel
            .leftJoin(RuleModel).apply {
                if (withStock) {
                    leftJoin(StockModel)
                    cols.add(StockModel.name)
                }
            }
            .slice(cols)
            .select {
                query and rule.isNotNull() and
                // If expanded we will show ALL rows included which were auto-generated
                // So we just put the query-param which won`t change the query res - rule.isNotNull()
                if (expanded) rule.isNotNull() else simplifiedBy.isNull()
            }
            .map {
                LinkedRuleOutputDto(
                    ruleId = it[rule]!!.value,
                    categoryId = it[RuleModel.category]?.value,
                    needStock = it[RuleModel.needStock],
                    stockId = it[stock]?.value
                ).apply {
                    if (withStock)
                        stockName = it[StockModel.name]
                }
            }
    }

    fun getRolesLinks(query: Op<Boolean>, withRules: Boolean, withStock: Boolean): List<RoleOutputDto> = transaction {
        RbacModel
            .leftJoin(RoleModel)
            .slice(role, RoleModel.name)
            .select {
                query and role.isNotNull()
            }
            .map {
                RoleOutputDto(it[role]!!.value, it[RoleModel.name])
                    //Add rules if we need to
                    .apply { if (withRules) rules = roleToRuleLinks(id, withStock) }
            }
    }


    //We select only rows which are not marked with simplified
    fun userToRuleLinks(userId: Int, withStock: Boolean = false, expanded: Boolean = false): List<LinkedRuleOutputDto> =
        getRuleLinks(
            user eq userId and simplifiedBy.isNull() and role.isNull(),
            withStock, expanded = expanded
        )

    fun roleToRuleLinks(roleId: Int, withStock: Boolean = false): List<LinkedRuleOutputDto> =
        getRuleLinks(
            role eq roleId and user.isNull(),
            withStock
        )

    fun userToRoleLinks(userId: Int, withRules: Boolean = false, withStock: Boolean = false): List<RoleOutputDto> = getRolesLinks(
        user eq userId, withRules, withStock
    )

    fun unlinkRules(from: Op<Boolean>, linkedRules: List<LinkedRuleInputDto>) = transaction {
        // Firstly remove rules which are haven`t stock id OR marked as forAll
        // We can delete them by one query
        val rulesWithoutStock = linkedRules.filter { it.stockId == null || it.forAll }.map { it.ruleId }
        RbacModel.deleteWhere {
            from and (rule inList rulesWithoutStock)
        }

        // Then start deleting by one item rules which have specified stock id
        linkedRules.filter { it.stockId != null }.forEach { linkedRule ->
            RbacModel.deleteWhere {
                from and (rule eq linkedRule.ruleId) and (stock eq linkedRule.stockId)
            }
        }
    }

    fun unlinkRoles(userId: Int, linkedRoles: List<Int>) = transaction {
        RbacModel.deleteWhere {
            (user eq userId) and (role inList linkedRoles)
        }
    }

    private data class RelationOnUpdate(
        val simplifiedBy: Int,
        val parent: Int,
        val child: Int,
        val isRole: Boolean
    )

    //We are search for user-rule-stock and role-rule-stock relations which are marked as "forAll" and creates new expanded rules
    fun updateSimplifiedRules(stockId: Int) = transaction {
        //Firstly look for user-rule relations
        val relations = RbacModel.slice(RbacModel.id, user, rule).select {
            stock eq stockId and forAll and role.isNull() and user.isNotNull()
        }.map {
            RelationOnUpdate(
                simplifiedBy = it[RbacModel.id].value,
                parent = it[user]!!.value,
                child = it[rule]!!.value,
                isRole = false
            )
        }.toMutableList()

        //Then for role-rule relations
        relations.addAll(
            RbacModel.slice(RbacModel.id, role, rule).select {
                stock eq stockId and forAll and role.isNotNull() and user.isNull()
            }.map {
                RelationOnUpdate(
                    simplifiedBy = it[RbacModel.id].value,
                    parent = it[role]!!.value,
                    child = it[rule]!!.value,
                    isRole = false
                )
            }
        )

        RbacModel.batchInsert(relations) {
            if (it.isRole)
                this[role] = it.parent
            else
                this[user] = it.parent

            this[rule] = it.child
            this[stock] = stockId
            this[simplifiedBy] = it.simplifiedBy
        }
    }
}