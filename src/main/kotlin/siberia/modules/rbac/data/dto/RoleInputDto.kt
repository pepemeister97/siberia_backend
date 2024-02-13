package siberia.modules.rbac.data.dto

import kotlinx.serialization.Serializable

@Serializable
abstract class RoleInputDto {
    abstract val name: String
    abstract val description: String?
    abstract val rules: List<LinkedRuleInputDto>
}