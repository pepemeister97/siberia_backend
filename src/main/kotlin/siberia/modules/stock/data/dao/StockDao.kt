package siberia.modules.stock.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.product.data.dao.ProductDao
import siberia.modules.stock.data.StockFullOutputDto
import siberia.modules.stock.data.dto.StockOutputDto
import siberia.modules.stock.data.dto.StockUpdateDto
import siberia.modules.stock.data.models.StockModel
import siberia.modules.stock.data.models.StockToProductModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class StockDao(id: EntityID<Int>): BaseIntEntity<StockOutputDto>(id, StockModel) {

    companion object: BaseIntEntityClass<StockOutputDto, StockDao>(StockModel)

    var name by StockModel.name
    var address by StockModel.address

    val products by ProductDao via StockToProductModel

    override fun toOutputDto(): StockOutputDto
        = StockOutputDto(idValue, name, address)

    fun loadUpdateDto(stockUpdateDto: StockUpdateDto) {
        name = stockUpdateDto.name ?: name
        address = stockUpdateDto.address ?: address
    }

    fun fullOutput(): StockFullOutputDto =
        StockFullOutputDto(idValue, name, address, products.map { it.toOutputDto() })
}