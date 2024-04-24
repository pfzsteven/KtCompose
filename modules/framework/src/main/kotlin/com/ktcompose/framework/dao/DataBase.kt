package com.ktcompose.framework.dao

import java.util.*

abstract class DataBase(private val operation: IDataBaseOperation) {

    abstract fun init(properties: Properties)

    fun opt(): IDataBaseOperation = operation
}

