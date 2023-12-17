package siberia.modules.transaction.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.transaction.data.dto.status.TransactionStatusOutputDto
import siberia.modules.transaction.data.models.TransactionStatusModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class TransactionStatusDao(id: EntityID<Int>) : BaseIntEntity<TransactionStatusOutputDto>(id, TransactionStatusModel) {
    companion object : BaseIntEntityClass<TransactionStatusOutputDto, TransactionStatusDao>(TransactionStatusModel)

    val name by TransactionStatusModel.name

    override fun toOutputDto(): TransactionStatusOutputDto =
        TransactionStatusOutputDto(idValue, name)
}