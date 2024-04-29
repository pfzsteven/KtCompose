package com.ktcompose.framework.dao

import com.ktcompose.framework.dao.mysql.MySqlDataBase
import java.util.*

object DaoFactory {

    private lateinit var db: DataBase

    fun init(properties: Properties) {
        val product = properties.getProperty("db.product", "mysql")
        when (product) {
            "mysql" -> {
                db = MySqlDataBase()
            }
        }
        db.init(properties)
    }

    suspend fun <T> executeTransaction(block: () -> T): T? {
        return db.opt().executeTransaction(block)
    }

    suspend fun <T> executeQuery(block: () -> T): T? {
        return db.opt().executeQuery(block)
    }
}