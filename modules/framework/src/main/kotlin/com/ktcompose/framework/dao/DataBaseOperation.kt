package com.ktcompose.framework.dao

import com.ktcompose.framework.utils.LogUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DataBaseOperation : IDataBaseOperation {

    override suspend fun <T> executeTransaction(block: () -> T): T? {
        var e: T? = null
        transaction {
            try {
                e = block()
                commit()
            } catch (t: Throwable) {
                LogUtils.d(DataBaseOperation::class.java, "===== executeTransaction exception[will rollback] =====")
                LogUtils.e(DataBaseOperation::class.java, t)
                rollback()
                e = null
            }
        }
        return e
    }

    override suspend fun <T> executeQuery(block: () -> T): T? {
        var e: T? = null
        transaction {
            try {
                e = block()
            } catch (t: Throwable) {
                LogUtils.e(DataBaseOperation::class.java, t)
            }
        }
        return e
    }
}