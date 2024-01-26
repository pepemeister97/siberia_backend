package siberia.modules.transaction.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object TransactionRelatedUserModel : BaseIntIdTable() {
    val user = reference("user", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val transaction = reference("transaction", TransactionModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)

    fun addRelated(userId: Int, transactionId: Int) = transaction {
        if (TransactionRelatedUserModel.select {
            (user eq userId) and (transaction eq transactionId)
        }.empty())
            TransactionRelatedUserModel.insert {
                it[user] = userId
                it[transaction] = transactionId
            }
    }

    fun getRelatedUsers(transactionId: Int): List<Int> = transaction {
        TransactionRelatedUserModel.select {
            (transaction eq transactionId)
        }.map { it[user].value }
    }
}