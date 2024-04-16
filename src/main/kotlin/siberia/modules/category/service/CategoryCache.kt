package siberia.modules.category.service

import siberia.modules.category.data.dto.CategoryOutputDto
import siberia.utils.database.CacheProvider

object CategoryCache: CacheProvider() {
    override var valid: Boolean = false
    private var categoryFullTree: List<CategoryOutputDto> = listOf()

    fun tryGetFullTree(providerFunction: () -> List<CategoryOutputDto>): List<CategoryOutputDto> =
        if (valid)
            categoryFullTree
        else {
            categoryFullTree = providerFunction()
            valid = true
            categoryFullTree
        }
    fun makeInvalid(){
        valid = false
    }
}