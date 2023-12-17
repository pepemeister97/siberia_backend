package siberia.modules.stock.data.models

import org.jetbrains.exposed.sql.*
import siberia.modules.transaction.data.dto.TransactionFullOutputDto
import siberia.utils.database.BaseIntIdTable
import siberia.utils.database.transaction

object StockModel: BaseIntIdTable() {
    val name = text("name")
    val address = text("address")

    private fun getProductsMapped(products: List<TransactionFullOutputDto.TransactionProductDto>): Pair<MutableMap<Int, Double>, List<Int>> =
        Pair(products.associate { Pair(it.product.id, it.amount) }.toMutableMap(), products.map { it.product.id })

    fun appendProducts(stockId: Int, products: List<TransactionFullOutputDto.TransactionProductDto>) = transaction {
        val productsMapped = getProductsMapped(products)
        val exist = StockToProductModel.select {
            (StockToProductModel.product inList productsMapped.second) and (StockToProductModel.stock eq stockId)
        }

        StockToProductModel.batchReplace(exist) {
            this[StockToProductModel.amount] = (it[StockToProductModel.amount] + (productsMapped.first[it[StockToProductModel.product].value] ?: 0.0))
            productsMapped.first.remove(it[StockToProductModel.product].value)
        }

        StockToProductModel.batchInsert(productsMapped.first.map { Pair(it.key, it.value) }) {
            this[StockToProductModel.stock] = stockId
            this[StockToProductModel.product] = it.first
            this[StockToProductModel.amount] = it.second
        }
    }

    fun checkAvailableAmount(stockId: Int, products: List<TransactionFullOutputDto.TransactionProductDto>) = transaction {
        val productsMapped = getProductsMapped(products).first
        val exist = StockToProductModel.select {
            (StockToProductModel.product inList products.map { it.product.id }) and (StockToProductModel.stock eq stockId)
        }.map {
            if (it[StockToProductModel.amount] - (productsMapped[it[StockToProductModel.product].value] ?: 0.0) <= 0.0)
                throw Exception("Not enough")
            it
        }

        if (exist.size < products.size)
            throw Exception("Not enough")
    }

    fun removeProducts(stockId: Int, products: List<TransactionFullOutputDto.TransactionProductDto>) = transaction {
        val productsMapped = getProductsMapped(products).first
        val exist = StockToProductModel.select {
            (StockToProductModel.product inList products.map { it.product.id }) and (StockToProductModel.stock eq stockId)
        }

        StockToProductModel.batchReplace(exist) {
            val resultAmount = (it[StockToProductModel.amount] - (productsMapped[it[StockToProductModel.product].value] ?: 0.0))
            if (resultAmount <= 0)
                throw Exception("Amount goes to negative!")
            else
                this[StockToProductModel.amount] = resultAmount
        }
    }
}