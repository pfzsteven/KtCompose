package com.ktcompose.framework.http

import com.ktcompose.framework.api.ApiCodesParser
import io.ktor.http.*

object HttpCodes {
    /**
     * invalid parameters
     */
    const val ERR_INVALID_PARAMETERS = -1000

    /**
     * unknown error
     */
    const val ERR_UNKNOWN = -2000

    /**
     * Account is not exists in DB.
     */
    const val ERR_ACCOUNT_NOT_EXISTS = -1001

    /**
     * User is not exists in DB
     */
    const val ERR_USER_NOT_EXISTS = -1002

    /**
     * Identity code (email code , sms code , etc)
     */
    const val ERR_INVALID_CODE = -1003

    @JvmStatic
    fun getMessage(code: HttpStatusCode, language: String? = "en"): String {
        return ApiCodesParser.codesMap[code.value]?.getMessage(language ?: "en") ?: ""
    }

    @JvmStatic
    fun getMessage(code: Int, language: String? = "en"): String {
        return ApiCodesParser.codesMap[code]?.getMessage(language ?: "en") ?: ""
    }
}