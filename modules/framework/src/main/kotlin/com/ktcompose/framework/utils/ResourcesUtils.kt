package com.ktcompose.framework.utils

import java.io.BufferedReader
import java.io.InputStream
import java.util.Properties

object ResourcesUtils {

    @JvmStatic
    fun readResourceAsText(resourceName: String): String? {
        val classLoader = Thread.currentThread().contextClassLoader
        val inputStream = classLoader.getResourceAsStream(resourceName)
        return inputStream?.bufferedReader()?.use(BufferedReader::readText)
    }

    @JvmStatic
    fun loadResourceFile(resourceName: String): InputStream? {
        val classLoader = Thread.currentThread().contextClassLoader
        try {
            val inputStream = classLoader.getResourceAsStream(resourceName)
            return inputStream
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun loadProperties(resourceName: String): Properties {
        val inputStream = object {}.javaClass.classLoader.getResourceAsStream(resourceName)
            ?: throw IllegalArgumentException("$resourceName file not found")
        val properties = Properties()
        properties.load(inputStream)
        inputStream.close()
        return properties
    }

    @JvmStatic
    fun hasResourceFile(filename: String): Boolean {
        val classLoader = Thread.currentThread().contextClassLoader
        try {
            val inputStream = classLoader.getResourceAsStream(filename)
            inputStream?.close()
            return true
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return false
    }
}