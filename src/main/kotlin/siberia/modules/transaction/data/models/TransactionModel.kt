package siberia.modules.transaction.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import siberia.conf.AppConf
import siberia.modules.product.data.dao.ProductDao
import siberia.modules.product.data.models.ProductModel
import siberia.modules.stock.data.models.StockModel
import siberia.modules.transaction.data.dao.TransactionDao
import siberia.modules.transaction.data.dto.TransactionFullOutputDto
import siberia.modules.transaction.data.dto.TransactionInputDto
import siberia.utils.database.BaseIntIdTable
import siberia.utils.database.idValue
import org.jetbrains.exposed.sql.transactions.transaction

object TransactionModel : BaseIntIdTable() {
    val from = reference("from", StockModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
    val to = reference("to", StockModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE).nullable().default(null)
    val status = reference("status", TransactionStatusModel, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
    val type = reference("type", TransactionTypeModel, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)

    fun create(transactionInputDto: TransactionInputDto): TransactionDao = transaction {
        val createdTransaction = TransactionDao.wrapRow(TransactionModel.insert {
            it[to] = transactionInputDto.to
            it[from] = transactionInputDto.from
            it[status] = AppConf.requestStatus.created
            it[type] = transactionInputDto.type
        }.resultedValues!!.first())

        TransactionToProductModel.batchInsert(transactionInputDto.products) {
            this[TransactionToProductModel.transaction] = createdTransaction.idValue
            this[TransactionToProductModel.product] = it.productId
            this[TransactionToProductModel.amount] = it.amount
            this[TransactionToProductModel.price] = it.price
        }

        createdTransaction
    }

    fun getFullProductList(transactionId: Int): List<TransactionFullOutputDto.TransactionProductDto> = transaction {
        val slice = ProductModel.columns.toMutableList()
        slice.add(TransactionToProductModel.amount)
        slice.add(TransactionToProductModel.price)
        TransactionToProductModel
            .leftJoin(ProductModel)
            .slice(slice)
            .select {
                TransactionToProductModel.transaction eq transactionId
            }
            .map {
                val productDao = ProductDao.wrapRow(it)
                TransactionFullOutputDto.TransactionProductDto(productDao.toOutputDto(), it[TransactionToProductModel.amount], it[TransactionToProductModel.price])
            }

    }
}