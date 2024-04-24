package com.ktcompose.framework.http

import com.ktcompose.framework.jwt.JwtManager

class HttpHeader : HashMap<String, String>() {

    companion object {
        const val KEY_IP = "ip"
        const val KEY_CLIENT_VERSION = "Client-Version"
        const val KEY_VERSION = "Version"
        const val KEY_BUILD = "Build"
        const val KEY_LANGUAGE = "Accept-Language"
        const val KEY_CLIENT_ID = "Client-Id"
    }

    fun getAuthorization(): String? {
        val jwtAuthName = JwtManager.httpHeaderName()
        return this[jwtAuthName] ?: this[jwtAuthName.lowercase()]
    }

    fun getClientIp(): String? {
        return this[KEY_IP]
    }

    fun getClientVersion(): String? {
        return this[KEY_CLIENT_VERSION] ?: this[KEY_CLIENT_VERSION.lowercase()]
    }

    fun getAppVersionName(): String? {
        return this[KEY_VERSION] ?: this[KEY_VERSION.lowercase()]
    }

    fun getVersionBuild(): String? {
        return this[KEY_BUILD] ?: this[KEY_BUILD.lowercase()]
    }

    fun getAcceptLanguage(): String? {
        return this[KEY_LANGUAGE] ?: this[KEY_LANGUAGE.lowercase()]
    }

    fun getClientId(): String? {
        return this[KEY_CLIENT_ID] ?: this[KEY_CLIENT_ID.lowercase()]
    }
}

class HttpRequestParameters : HashMap<String, Any>()

data class HttpRequest(val header: HttpHeader, val parameters: HttpRequestParameters)