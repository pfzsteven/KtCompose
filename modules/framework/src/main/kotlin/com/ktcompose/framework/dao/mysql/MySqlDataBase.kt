package com.ktcompose.framework.dao.mysql

import com.ktcompose.framework.dao.DataBase
import com.ktcompose.framework.dao.DataBaseOperation
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.util.*

/**
 * MySQL
 */
class MySqlDataBase : DataBase(DataBaseOperation()) {

    private var connected: Boolean = false
    private lateinit var dataSource: HikariDataSource

    override fun init(properties: Properties) {
        if (connected) return
        val host = properties.getProperty("db.host") ?: "localhost"
        val port = properties.getProperty("db.port", "3306").toInt()
        val user = properties.getProperty("db.user") ?: ""
        val pwd = properties.getProperty("db.pwd") ?: ""
        val schema = properties.getProperty("db.schema") ?: ""
        val url = "jdbc:mysql://$host:$port/$schema?useUnicode=true&serverTimezone=UTC"
        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = url
            username = user
            password = pwd
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.addDataSourceProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        connected = true
        println("----- mysql connect successfully ----")
    }


}