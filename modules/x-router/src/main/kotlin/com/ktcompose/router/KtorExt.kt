package com.ktcompose.router

import com.ktcompose.framework.http.HttpRequestHeader
import com.ktcompose.framework.http.HttpRequestParameters
import com.ktcompose.framework.utils.GsonUtils
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json

object KtorExt {

    @JvmStatic
    fun Application.configureHTTP() {
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.AccessControlAllowHeaders)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.ContentLength)
            allowHeader(HttpHeaders.ContentDisposition)
            allowHeader(HttpHeaders.AccessControlAllowOrigin)
            allowHeadersPrefixed("X-")
            allowCredentials = true
            exposeHeader(HttpHeaders.AccessControlAllowOrigin)
        }
        install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
        install(XForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
        install(DefaultHeaders) {
            header("X-Engine", "Ktor") // will send this header with each response
        }
        install(AutoHeadResponse)
        install(DoubleReceive)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Compression) {
            gzip()
            deflate()
        }
    }

    @JvmStatic
    suspend fun ApplicationCall.filter(
        result: suspend (header: HttpRequestHeader, param: HttpRequestParameters) -> Unit
    ) {
        val httpRequestHeader = HttpRequestHeader()
        var httpRequestParameters: HttpRequestParameters? = null
        val headers: Headers = request.headers
        headers.forEach { key, values ->
            val valueBuilder = StringBuilder()
            values.forEach { v ->
                valueBuilder.append(v).append(",")
            }
            if (valueBuilder.isNotEmpty()) {
                valueBuilder.deleteCharAt(valueBuilder.length - 1)
            }
            httpRequestHeader[key] = valueBuilder.toString()
        }
        when (request.contentType()) {
            ContentType.Application.Json -> {
                this.receiveText().takeIf { it.isNotEmpty() }?.let { jsonText ->
                    httpRequestParameters = GsonUtils.gson.fromJson(jsonText, HttpRequestParameters::class.java)
                }
            }

            ContentType.Application.Any, ContentType.Application.FormUrlEncoded -> {
                val rawParams: Parameters = this.receiveParameters()
                httpRequestParameters = HttpRequestParameters()
                httpRequestParameters?.putAll(rawParams.toHttpRequestParameters())
            }
        }
        httpRequestParameters = httpRequestParameters ?: HttpRequestParameters()
        result.invoke(httpRequestHeader, httpRequestParameters!!)
    }

    @JvmStatic
    private fun Parameters.toHttpRequestParameters(): HttpRequestParameters {
        val p = HttpRequestParameters()
        this.forEach { key, values ->
            val valueBuilder = StringBuilder()
            values.forEach { v ->
                valueBuilder.append(v).append(",")
            }
            if (valueBuilder.isNotEmpty()) {
                valueBuilder.deleteCharAt(valueBuilder.length - 1)
            }
            p[key] = valueBuilder.toString()
        }
        return p
    }
}