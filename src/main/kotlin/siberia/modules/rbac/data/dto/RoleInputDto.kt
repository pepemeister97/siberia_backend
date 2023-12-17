package siberia.modules.rbac.data.dto

data class RoleInputDto (
    val name: String,
    val description: String? = null,
    val linkedRuleInputDto: List<LinkedRuleInputDto> = listOf()
)