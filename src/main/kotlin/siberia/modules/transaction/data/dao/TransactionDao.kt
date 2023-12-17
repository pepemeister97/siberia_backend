package siberia.modules.transaction.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.product.data.dao.ProductDao
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.transaction.data.dto.TransactionFullOutputDto
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.modules.transaction.data.dto.TransactionListItemOutputDto
import siberia.modules.transaction.data.dto.TransactionOutputDto
import siberia.modules.transaction.data.models.TransactionModel
import siberia.modules.transaction.data.models.TransactionToProductModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class TransactionDao(id: EntityID<Int>) : BaseIntEntity<TransactionOutputDto>(id, TransactionModel) {
    companion object : BaseIntEntityClass<TransactionOutputDto, TransactionDao>(TransactionModel)

    private val _fromId by TransactionModel.from
    val fromId = _fromId?.value
    val from by StockDao optionalReferencedOn TransactionModel.from

    private val _toId by TransactionModel.to
    val toId = _toId?.value
    val to by StockDao optionalReferencedOn TransactionModel.to

    private val _statusId by TransactionModel.status
    val statusId = _statusId.value
    var status by TransactionStatusDao referencedOn TransactionModel.status

    private val _typeId by TransactionModel.type
    val typeId = _typeId.value
    val type by TransactionTypeDao referencedOn TransactionModel.type

    val products by ProductDao via TransactionToProductModel

    override fun toOutputDto(): TransactionOutputDto =
        TransactionOutputDto(idValue, fromId, toId, statusId, typeId)

    fun toInputDto(): TransactionInputDto =
        TransactionInputDto(fromId, toId, typeId, listOf())

    fun listItemOutputDto(): TransactionListItemOutputDto =
        TransactionListItemOutputDto(
            idValue, fromId, from?.name,
            toId, to?.name,
            status.toOutputDto(),
            type.toOutputDto()
        )

    fun fullOutput(): TransactionFullOutputDto =
        TransactionFullOutputDto(
            idValue, from?.toOutputDto(),
            to?.toOutputDto(),
            status.toOutputDto(),
            type.toOutputDto(),
            TransactionModel.getFullProductList(idValue)
        )

}