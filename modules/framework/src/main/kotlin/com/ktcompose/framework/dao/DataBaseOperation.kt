package com.ktcompose.framework.dao

import com.ktcompose.framework.utils.LogUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DataBaseOperation : IDataBaseOperation {

    override suspend fun <T> executeTransaction(block: () -> T) {
        transaction {
            try {
                block()
                commit()
            } catch (t: Throwable) {
                LogUtils.d(DataBaseOperation::class.java, "===== executeTransaction exception[will rollback] =====")
                LogUtils.exception(DataBaseOperation::class.java, t)
                rollback()
            }
        }
    }

    override suspend fun <T> executeQuery(block: () -> T) {
        block()
    }
}