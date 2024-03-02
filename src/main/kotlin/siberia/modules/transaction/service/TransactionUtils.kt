package siberia.modules.transaction.service

import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import siberia.modules.transaction.data.dao.TransactionDao

object TransactionUtils {

    /*
        This method takes type of the request and status
        that user wants to set and returns the rules, which user must have to set corresponding status.
    */
    fun mapTypeToRule(typeId: Int, statusId: Int): Int =
        when (typeId) {
            AppConf.requestTypes.income -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createIncomeRequest
                    }
                    AppConf.requestStatus.creationCancelled -> {
                        AppConf.rules.approveIncomeRequest
                    }
                    AppConf.requestStatus.processed -> {
                        AppConf.rules.approveIncomeRequest
                    }
                    else -> {
                        throw BadRequestException("Bad request status")
                    }
                }
            }
            AppConf.requestTypes.outcome -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createOutcomeRequest
                    }
                    AppConf.requestStatus.open -> {
                        AppConf.rules.createOutcomeRequest
                    }
                    AppConf.requestStatus.creationCancelled -> {
                        AppConf.rules.approveOutcomeRequest
                    }
                    AppConf.requestStatus.processed -> {
                        AppConf.rules.approveOutcomeRequest
                    }
                    else -> {
                        throw BadRequestException("Bad request status")
                    }
                }
            }
            AppConf.requestTypes.transfer -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createTransferRequest
                    }
                    AppConf.requestStatus.creationCancelled -> {
                        AppConf.rules.approveTransferRequestCreation
                    }
                    AppConf.requestStatus.open -> {
                        AppConf.rules.approveTransferRequestCreation
                    }
                    AppConf.requestStatus.processingCancelled -> {
                        AppConf.rules.manageTransferRequest
                    }
                    AppConf.requestStatus.inProgress -> {
                        AppConf.rules.manageTransferRequest
                    }
                    AppConf.requestStatus.delivered -> {
                        AppConf.rules.approveTransferDelivery
                    }
                    AppConf.requestStatus.notDelivered -> {
                        AppConf.rules.approveTransferDelivery
                    }
                    AppConf.requestStatus.failed -> {
                        AppConf.rules.solveNotDeliveredProblem
                    }
                    AppConf.requestStatus.deliveryCancelled -> {
                        AppConf.rules.solveNotDeliveredProblem
                    }
                    else -> {
                        throw BadRequestException("Bad request status")
                    }
                }
            }
            AppConf.requestTypes.writeOff -> {
                when (statusId) {
                    AppConf.requestStatus.created -> {
                        AppConf.rules.createWriteOffRequest
                    }
                    AppConf.requestStatus.creationCancelled -> {
                        AppConf.rules.approveWriteOffRequest
                    }
                    AppConf.requestStatus.processed -> {
                        AppConf.rules.approveWriteOffRequest
                    }
                    else -> {
                        throw BadRequestException("Bad request status")
                    }
                }
            }
            else -> {
                throw BadRequestException("Bad request type")
            }
        }

    fun availableStatuses(transactionDao: TransactionDao) = AppConf.requestStatusMapper[transactionDao.typeId]?.get(transactionDao.statusId) ?: listOf()
}