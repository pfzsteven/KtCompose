package com.ktcompose.router

import com.ktcompose.framework.api.dto.ApiResponse
import com.ktcompose.framework.http.HttpCodes
import com.ktcompose.framework.http.HttpHeader
import com.ktcompose.framework.http.HttpRequest
import com.ktcompose.framework.jwt.JwtManager
import com.ktcompose.framework.utils.GsonUtils
import com.ktcompose.framework.utils.LogUtils
import com.ktcompose.framework.utils.ResourcesUtils
import com.ktcompose.framework.utils.XmlUtils
import com.ktcompose.router.KtorExt.filter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 默认路由处理类
 * <外观模式>:对外暴露统一函数 `handle`，对外提供简单化的调用。内部进行复杂请求处理
 */
object RouterHandler {

    private val routerMap: ConcurrentHashMap<String, Router> by lazy { ConcurrentHashMap() }

    fun filter(method: HttpMethod, each: (path: String, Router) -> Unit) {
        val m = method.value.lowercase()
        val map = HashMap<String, Router>()
        routerMap.forEach { (p, router) ->
            if (router.method?.lowercase() == m) {
                map[p] = router
            }
        }
        map.forEach(each)
    }

    private fun registerRoutersFromXml() {
        ResourcesUtils.loadResourceFile("routers.xml")?.let { inputStream ->
            XmlUtils.parse(inputStream) { node ->
                RouterXmlParser.parse(node, routerMap)
            }
        }
    }

    private fun registerRoutersFromUrl() {
    }

    fun init() {
        registerRoutersFromXml()
        registerRoutersFromUrl()
        if (LogUtils.enable()) {
            routerMap.forEach { (s, router) ->
                LogUtils.i(RouterHandler::class.java, "Router[${s}]====${router.path}")
            }
        }
    }

    private suspend fun checkAuthorization(
        router: Router, path: String, authorization: String?
    ): Boolean {
        return !(router.needAuthorization && !JwtManager.verify(path, authorization))
    }

    suspend fun handle(
        call: ApplicationCall, router: Router
    ) {
        val path = call.request.path()
        val clientIp = call.request.origin.remoteHost
        if (!routerMap.containsKey(path)) {
            val errorMsg = HttpCodes.getMessage(HttpStatusCode.Unauthorized)
            call.respondText(
                errorMsg, ContentType.Application.Json, HttpStatusCode.NotFound
            )
            return
        }
        call.filter(router) { headers, params ->
            headers[HttpHeader.KEY_IP] = clientIp
            val authorization = headers.getAuthorization()
            if (!checkAuthorization(router, path, authorization)) {
                call.respondText(
                    HttpCodes.getMessage(HttpStatusCode.Unauthorized),
                    ContentType.Application.Json,
                    HttpStatusCode.Unauthorized
                )
                return@filter
            }
            if (!JwtManager.verifyPermission(authorization, router.permission)) {
                call.respondText(
                    HttpCodes.getMessage(HttpStatusCode.Forbidden),
                    ContentType.Application.Json,
                    HttpStatusCode.Forbidden
                )
                return@filter
            }
            router.handler?.let { handler ->
                handler.params.forEach { p ->
                    if (!p.canBeNull && (p.name.isNullOrEmpty() || !params.containsKey(p.name))) {
                        val errorMsg = HttpCodes.getMessage(HttpCodes.ERR_INVALID_PARAMETERS)
                        call.respondText(
                            errorMsg, ContentType.Application.Json, HttpStatusCode.OK
                        )
                        return@filter
                    }
                }
                handler.dispatch { method, instance, args ->
                    runBlocking {
                        args.addFirst(HttpRequest(headers, params))
                        args.addLast(object : Continuation<String> {
                            override val context: CoroutineContext = EmptyCoroutineContext
                            override fun resumeWith(result: Result<String>) {
                            }
                        })
                        val apiResponse = method.invoke(instance, *args.toTypedArray())?.takeIf {
                            it is ApiResponse<*>
                        }?.let { dto ->
                            dto as ApiResponse<*>
                        }
                        if (apiResponse == null) {
                            call.respondText(
                                "${router.path}", ContentType.Application.Json, HttpStatusCode.InternalServerError
                            )
                            return@runBlocking
                        }
                        apiResponse.header?.takeIf { it.isNotEmpty() }?.let { httpHeader ->
                            httpHeader.forEach { (k, v) ->
                                call.response.header(k, v)
                            }
                        } ?: apply {
                            authorization?.let { token ->
                                // add header
                                call.response.header(JwtManager.httpHeaderName(), token)
                            }
                        }
                        call.respondText(
                            GsonUtils.gson.toJson(apiResponse), ContentType.Application.Json, HttpStatusCode.OK
                        )
                    }
                }
            } ?: call.respondText(
                "RouterDispatchHandler is not found.", ContentType.Application.Json, HttpStatusCode.InternalServerError
            )
        }
    }
}