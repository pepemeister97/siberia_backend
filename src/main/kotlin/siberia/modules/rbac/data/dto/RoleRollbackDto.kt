package siberia.modules.rbac.data.dto

data class RoleRollbackDto (
    override val name: String,
    override val description: String? = null,
    override val rules: List<LinkedRuleInputDto> = listOf(),
    val relatedUsers: List<Pair<Int, String>> = listOf()
): RoleInputDto()