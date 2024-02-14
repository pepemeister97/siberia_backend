package siberia.modules.stock.service

import siberia.modules.stock.data.dto.StockOutputDto
import siberia.utils.database.CacheProvider

object StockCache: CacheProvider() {
    override var valid: Boolean = false
    private var stockInputList: List<StockOutputDto> = listOf()

    fun tryGetAllInput(providerFunction: () -> List<StockOutputDto>): List<StockOutputDto> =
        if (valid)
            stockInputList
        else {
            stockInputList = providerFunction()
            valid = true
            stockInputList
        }
}