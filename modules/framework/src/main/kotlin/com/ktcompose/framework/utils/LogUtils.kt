package com.ktcompose.framework.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object LogUtils {

    private var isEnabled: Boolean = false
    private val loggerFactory: ConcurrentHashMap<String, Logger> by lazy { ConcurrentHashMap<String, Logger>() }
    private fun getOrCreate(cls: Class<*>): Logger {
        val key = cls.name
        return if (loggerFactory.containsKey(key)) {
            loggerFactory[key]!!
        } else {
            LoggerFactory.getLogger(cls).let {
                loggerFactory[key] = it
                it
            }
        }
    }

    @JvmStatic
    fun enable() = isEnabled;

    @JvmStatic
    fun init(config: String?) {
        config?.let {
            isEnabled = (it == "true")
        }
        if (isEnabled) {
            println("Logger is enable.")
        } else {
            println("Logger is disabled.")
        }
    }

    @JvmStatic
    fun d(tag: Class<*>, msg: String) {
        getOrCreate(tag).debug(msg)
    }

    @JvmStatic
    fun i(tag: Class<*>, msg: String) {
        getOrCreate(tag).info("[$tag]: $msg")
    }

    @JvmStatic
    fun w(tag: Class<*>, msg: String) {
        getOrCreate(tag).warn(msg)
    }

    @JvmStatic
    fun w(tag: Class<*>, e: Exception) {
        getOrCreate(tag).warn(e.message, e)
    }

    @JvmStatic
    fun e(tag: Class<*>, msg: String) {
        if (enable()) {
            getOrCreate(tag).error(msg)
        }
    }

    @JvmStatic
    fun exception(tag: Class<*>, t: Throwable) {
        t.printStackTrace()
        getOrCreate(tag).error("${t.message}", t)
    }
}