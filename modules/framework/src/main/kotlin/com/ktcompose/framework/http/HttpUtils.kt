package com.ktcompose.framework.http

import com.ktcompose.framework.utils.LogUtils
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * todo: https://ktor.io/docs/request.html#upload_file
 */
object HttpUtils {

    private var connection_timeout: Long = 30_000
    private var read_timeout: Long = 30_000

    private val loggingInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val request: Request = chain.request()
            if (LogUtils.enable()) {
                val url = request.url.toString()
                val logBuilder = StringBuilder()
                logBuilder.append("$url -> ${request.method}\n")
                LogUtils.i(HttpUtils::class.java, logBuilder.toString())
            }
            chain.proceed(request)
        }
    }

    private val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    followRedirects(true)
                    callTimeout(connection_timeout, TimeUnit.MILLISECONDS)
                    readTimeout(read_timeout, TimeUnit.MILLISECONDS)
                }
                addNetworkInterceptor(loggingInterceptor)
            }
        }
    }

    fun init(properties: Properties) {
        val connectionTimeout = properties.getProperty("http.connect_timeout")
        val readTimeout = properties.getProperty("http.read_timeout")
        connectionTimeout?.let {
            connection_timeout = it.toInt() * 1000L
        }
        readTimeout?.let {
            read_timeout = it.toInt() * 1000L
        }
    }

    suspend fun get(
        url: String, headers: Map<String, String> = emptyMap(), params: Map<String, String> = emptyMap()
    ): HttpResult {
        val rawData = HttpResult()
        runBlocking {
            try {
                val urlBuilder: StringBuilder = StringBuilder(url)
                if (params.isNotEmpty()) {
                    if (!url.contains("?")) {
                        urlBuilder.append("?")
                    } else {
                        urlBuilder.append("&")
                    }
                    params.forEach { (key, value) ->
                        urlBuilder.append(key).append("=").append(value).append("&")
                    }
                    urlBuilder.removeRange(urlBuilder.length - 1, urlBuilder.length)
                }
                val response: HttpResponse = client.get(urlBuilder.toString()) {
                    headers.forEach { (key, value) ->
                        header(key, value)
                    }
                }
                rawData.text = response.bodyAsText()
                rawData.statusCode = response.status
            } catch (e: Exception) {
                LogUtils.e(HttpUtils::class.java, "Http Error.[get] url:$url failed.")
                LogUtils.e(HttpUtils::class.java, e)
                rawData.exception = e
            }
        }
        return rawData
    }

    suspend fun post(
        url: String, headers: Map<String, String> = emptyMap(), params: Map<String, String> = emptyMap()
    ): HttpResult {
        if (LogUtils.enable()) {
            val logBuilder = StringBuilder("[POST]\n")
            params.forEach { (key, value) ->
                logBuilder.append("\t").append(key).append("=>").append(value).append("\n")
            }
            LogUtils.i(HttpUtils::class.java, logBuilder.toString())
        }
        val rawData = HttpResult()
        runBlocking {
            try {
                val response: HttpResponse = client.submitForm(url, formParameters = parameters {
                    params.forEach { (key, value) ->
                        append(key, value)
                    }
                }) {
                    headers.forEach { (key, value) ->
                        header(key, value)
                    }
                    contentType(ContentType.Application.Json)
                }
                rawData.text = response.bodyAsText()
                rawData.statusCode = response.status
            } catch (e: Exception) {
                LogUtils.e(HttpUtils::class.java, "Http Error.[post] url:$url failed.")
                LogUtils.e(HttpUtils::class.java, e)
                rawData.exception = e
            }
        }
        return rawData
    }
}