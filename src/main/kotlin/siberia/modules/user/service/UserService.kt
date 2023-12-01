package siberia.modules.user.service

import org.kodein.di.DI
import siberia.exceptions.ValidateException
import siberia.modules.auth.data.dao.RoleDao
import siberia.modules.auth.data.dao.RuleDao
import siberia.modules.auth.data.dao.UserToRuleDao
import siberia.modules.auth.data.dto.LinkedRuleInputDto
import siberia.modules.auth.data.dto.LinkedRuleOutputDto
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.dto.AuthorizedUser
import siberia.utils.kodein.KodeinService

class UserService(di: DI) : KodeinService(di) {
    fun addRuleToUser(userDao: UserDao, ruleId: Int, stockId: Int? = null) {
        val ruleDao = RuleDao[ruleId]
        UserToRuleDao.new {
            user = userDao
            rule = ruleDao
            if (rule.needStock) {
                if (stockId != null)
                    stock = StockDao[stockId]
                else
                    throw ValidateException.build {
                        addError(ValidateException.ValidateError("stock_id", "must be provided"))
                    }
            }
        }
    }

    fun addRoleToUser(userDao: UserDao, roleId: Int): List<LinkedRuleOutputDto> {
        val role = RoleDao[roleId]
        return role.outputWithChildren.rules.map {
            addRuleToUser(userDao, it.ruleId, it.stockId)
            LinkedRuleOutputDto(it.ruleId, it.stockId)
        }
    }

    fun addRules(authorizedUser: AuthorizedUser, newRules: List<LinkedRuleInputDto>): List<LinkedRuleOutputDto> {
        val userDao = UserDao[authorizedUser.id]
        return newRules.map {
            addRuleToUser(userDao, it.ruleId, it.stockId)
            LinkedRuleOutputDto(it.ruleId, it.stockId)
        }
    }

    fun addRoles(authorizedUser: AuthorizedUser, newRoles: List<Int>): List<LinkedRuleOutputDto> {
        val userDao = UserDao[authorizedUser.id]
        return newRoles.map {
            addRoleToUser(userDao, it)
        }.flatten()
    }
}