package com.ktcompose.framework.http

import io.ktor.http.*

class HttpResult {
    var statusCode: HttpStatusCode = HttpStatusCode.Accepted
    var text: String? = null
    var exception: Throwable? = null

    companion object {
        @JvmStatic
        fun error(code: Int): HttpResult {
            return HttpResult().apply {
                statusCode = HttpStatusCode.fromValue(code)
            }
        }
    }

    override fun toString(): String {
        return "HttpResult:\n--statusCode:$statusCode \n--data:$text \n--exception:${exception?.message}"
    }
}
