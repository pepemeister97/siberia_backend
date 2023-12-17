package siberia.modules.transaction.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.transaction.data.dto.type.TransactionTypeOutputDto
import siberia.modules.transaction.data.models.TransactionTypeModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class TransactionTypeDao(id: EntityID<Int>) : BaseIntEntity<TransactionTypeOutputDto>(id, TransactionTypeModel) {
    companion object : BaseIntEntityClass<TransactionTypeOutputDto, TransactionTypeDao>(TransactionTypeModel)

    val name by TransactionTypeModel.name

    override fun toOutputDto(): TransactionTypeOutputDto =
        TransactionTypeOutputDto(idValue, name)
}