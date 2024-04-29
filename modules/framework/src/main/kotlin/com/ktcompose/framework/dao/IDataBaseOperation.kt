package com.ktcompose.framework.dao

interface IDataBaseOperation {

    suspend fun <T> executeTransaction(block: () -> T): T?

    suspend fun <T> executeQuery(block: () -> T): T?
}