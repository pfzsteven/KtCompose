package com.ktcompose.framework.api.dto

import com.ktcompose.framework.http.HttpHeader

data class ApiResponse<T>(private val serializedId: Long = 1L) {

    /**
     * 状态码: 0或者200 代表无错误
     */
    var code: Int = 0

    /**
     * 数据
     */
    var data: T? = null

    /**
     * 错误消息等
     */
    var msg: String? = null

    /**
     * 响应头添加自定义属性等
     */
    @Transient
    var header: HttpHeader? = null

    /**
     * 往响应头添加数据
     */
    fun addHead(name: String, value: String?): ApiResponse<T> {
        value?.let { v ->
            header = header ?: HttpHeader()
            header?.apply {
                put(name, v)
            }
        }
        return this
    }

    companion object {

        @JvmStatic
        var autoSerialNumber: Long = 100000000L

        @JvmStatic
        fun updateSerialNumber(serializedId: String) {
            serializedId.takeIf { it.isNotEmpty() }?.let {
                autoSerialNumber = it.toLong()
            }
        }

        @JvmStatic
        inline fun <reified T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse<T>(autoSerialNumber).let { result ->
                result.code = 0
                result.data = data
                result.msg = "success"
                result
            }
        }

        @JvmStatic
        inline fun <reified T> fail(code: Int, msg: String?): ApiResponse<T> {
            return ApiResponse<T>(autoSerialNumber).let { result ->
                result.code = code
                result.msg = msg
                result
            }
        }
    }
}