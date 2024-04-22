package com.ktcompose.framework.http

import com.ktcompose.framework.jwt.JwtManager

/**
 * Http请求头
 */
class HttpHeader : HashMap<String, String>() {

    /**
     * 获取jwt 鉴权信息
     */
    fun getAuthorization(): String? {
        val jwtAuthName = JwtManager.tokenName()
        return this[jwtAuthName] ?: this[jwtAuthName.lowercase()]
    }
}

/**
 * Http请求参数
 */
class HttpRequestParameters : HashMap<String, Any>()

/**
 * 封装Http请求对象
 */
data class HttpRequest(val header: HttpHeader, val parameters: HttpRequestParameters)