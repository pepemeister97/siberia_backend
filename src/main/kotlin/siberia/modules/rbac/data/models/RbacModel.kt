package siberia.modules.rbac.data.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.transaction
import siberia.modules.rbac.data.dto.LinkedRuleInputDto
import siberia.modules.rbac.data.dto.LinkedRuleOutputDto
import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.rbac.data.models.role.RoleModel
import siberia.modules.rbac.data.models.rule.RuleModel
import siberia.modules.stock.data.models.StockModel
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntIdTable

object RbacModel: BaseIntIdTable() {
    val user = reference("user", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
    val role = reference("role", RoleModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
    val rule = reference("rule", RuleModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
    val stock = reference("stock", StockModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)

    // it links on "main" row which is simplified this
    // field means that current row is created to expand some relations:
    // 1) user-role relation to be the many user-rule relations
    // 2) user-rule-stock and role-rule-stock (forAll) to be the manu user-rule-stocks and role-rule-stocks relations
    val simplifiedBy = reference("simplified_by", RbacModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)

    //Future iterations
    // Field is created for user-rule-stock and role-rule-stock (rules with stock) relations
    // it means that user have access to all stocks by this rule ex. can view all stocks
    // For such rule will be automatically generated all user-rule-stocks relations and marked as simplifiedBy this one
    //val forAll = bool("for_all").default(false)

    fun getRuleLinks(query: Op<Boolean>, withStock: Boolean, expanded: Boolean = false): List<LinkedRuleOutputDto> = transaction {
        val cols = mutableListOf<Expression<*>>(rule, stock, RuleModel.category, RuleModel.needStock)
        RbacModel.let {
            if (withStock) {
                cols.add(StockModel.name)
                it.leftJoin(StockModel)
            } else {
                it
            }
        }
        .leftJoin(RuleModel)
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
                if (withStock && it[stock] != null)
                    stockName = it[StockModel.name]
            }
        }
    }

    fun getRolesLinks(query: Op<Boolean>, withRules: Boolean, withStock: Boolean): List<RoleOutputDto> = transaction {
        RbacModel
            .leftJoin(RoleModel)
            .slice(role, RoleModel.name, RoleModel.description)
            .select {
                query and role.isNotNull()
            }
            .map {
                RoleOutputDto(it[role]!!.value, it[RoleModel.name], it[RoleModel.description])
                    //Add rules if we need to
                    .apply { if (withRules) rules = roleToRuleLinks(id, withStock) }
            }
    }


    //Expanded param:
    //If it true it means we will select all rules included auto-generated and marked as simplifiedBy
    //If it false we will select only rules which are related to user without auto-generated ones
    fun userToRuleLinks(userId: Int, withStock: Boolean = false, expanded: Boolean = false): List<LinkedRuleOutputDto> =
        getRuleLinks(
            user eq userId and if (expanded) rule.isNotNull() else simplifiedBy.isNull() and role.isNull(),
            withStock, expanded = expanded
        ).distinctBy { Pair(it.ruleId, it.stockId) }

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
        val rulesWithoutStock = linkedRules.filter { it.stockId == null }.map { it.ruleId }
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

    fun getRelatedUsers(roleId: Int) = RbacModel
        .leftJoin(UserModel)
        .slice(id, user, role, stock, rule, simplifiedBy, UserModel.id, UserModel.name)
        .select {
            (role eq roleId) and (user.isNotNull())
        }

    fun expandAppendedRules(roleId: Int, linkedRules: List<LinkedRuleOutputDto>) {
        val onInsert = getRelatedUsers(roleId).mapNotNull { if (it[user] == null) null else Pair(it[user]!!.value, it[id].value) }.map {
            Pair(it, linkedRules)
        }.flatMap { row ->
            row.second.map { Pair(row.first, it) }
        }

        RbacModel.batchInsert(onInsert) {
            this[user] = it.first.first
            this[rule] = it.second.ruleId
            this[stock] = it.second.stockId
            this[simplifiedBy] = it.first.second
        }
    }

    fun removeExpandedRules(roleId: Int, linkedRules: List<LinkedRuleInputDto>) {
        val simplifiedRowsIds = RbacModel.select {
            (role eq roleId) and (user.isNotNull())
        }.map { it[id] }
        linkedRules.map { row ->
            RbacModel.deleteWhere {
                (rule eq row.ruleId) and (stock eq row.stockId) and (simplifiedBy inList simplifiedRowsIds)
            }
        }
    }
}