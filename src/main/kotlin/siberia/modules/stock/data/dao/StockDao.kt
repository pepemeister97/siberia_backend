package siberia.modules.stock.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.stock.data.dto.StockOutputDto
import siberia.modules.stock.data.models.StockModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class StockDao(id: EntityID<Int>): BaseIntEntity<StockOutputDto>(id, StockModel) {

    companion object: BaseIntEntityClass<StockOutputDto, StockDao>(StockModel)

    val name by StockModel.name
    val address by StockModel.address

    override fun toOutputDto(): StockOutputDto
        = StockOutputDto(idValue, name, address)
}