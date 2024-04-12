package com.ktcompose.framework.http

import com.ktcompose.framework.api.ApiCodesParser
import io.ktor.http.*

object HttpCodes {
    /**
     * 请求参数错误
     */
    const val ERR_INVALID_PARAMETERS = -1000

    /**
     * 权限不足
     */
    const val ERR_NO_PERMISSIONS = -2000

    @JvmStatic
    fun getMessage(code: HttpStatusCode, language: String? = "en"): String {
        return ApiCodesParser.codesMap[code.value]?.getMessage(language ?: "en") ?: ""
    }

    @JvmStatic
    fun getMessage(code: Int, language: String? = "en"): String {
        return ApiCodesParser.codesMap[code]?.getMessage(language ?: "en") ?: ""
    }
}