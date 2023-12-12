package siberia.modules.auth.data.dto

data class RoleInputDto (
    val name: String,
    val linkedRuleInputDto: List<LinkedRuleInputDto> = listOf()
)