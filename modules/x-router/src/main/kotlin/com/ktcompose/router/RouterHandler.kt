package com.ktcompose.router

import com.ktcompose.framework.http.HttpCodes
import com.ktcompose.framework.http.HttpResult
import com.ktcompose.framework.utils.LogUtils
import com.ktcompose.framework.utils.ResourcesUtils
import com.ktcompose.framework.utils.XmlUtils
import com.ktcompose.jwt.JwtManager
import com.ktcompose.router.KtorExt.filter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 默认路由处理类(todo 支持xml指定自定义)
 */
object RouterHandler {

    private val routerMap: ConcurrentHashMap<String, Router> by lazy { ConcurrentHashMap() }
    private val continuation by lazy {
        object : Continuation<String> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<String>) {
            }
        }
    }

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
        routerMap.forEach { (s, router) ->
            println("Router[${s}]====${router.path}")
        }
    }

    suspend fun handle(
        call: ApplicationCall, router: Router
    ) {

        val path = call.request.path()
        if (!routerMap.containsKey(path)) {
            val errorMsg = HttpCodes.getMessage(HttpStatusCode.Unauthorized)
            LogUtils.e(RouterHandler::class.java, errorMsg)
            call.respondText(
                errorMsg, ContentType.Application.Json, HttpStatusCode.NotFound
            )
            return
        }
        // 接口参数过滤
        call.filter { headers, params ->
            if (LogUtils.enable()) {
                params.forEach { (k, v) ->
                    LogUtils.i(RouterHandler::class.java, "params[$k]=$v")
                }
            }
            // 鉴权判断
            if (router.needAuthorization) {
                val authorization = headers[HttpHeaders.Authorization] ?: headers[HttpHeaders.Authorization.lowercase()]
                if (authorization.isNullOrEmpty() || !JwtManager.checkValid(authorization)) {
                    val errorMsg = HttpCodes.getMessage(HttpStatusCode.Unauthorized)
                    LogUtils.e(RouterHandler::class.java, errorMsg)
                    call.respondText(
                        errorMsg, ContentType.Application.Json, HttpStatusCode.OK
                    )
                    return@filter
                }
            }
            // 参数校验
            router.params?.forEach { p ->
                if (!p.canBeNull && (p.name.isNullOrEmpty() || !params.containsKey(p.name))) {
                    val errorMsg = HttpCodes.getMessage(HttpCodes.ERR_INVALID_PARAMETERS)
                    LogUtils.e(RouterHandler::class.java, errorMsg)
                    call.respondText(
                        errorMsg, ContentType.Application.Json, HttpStatusCode.OK
                    )
                    return@filter
                }
            }
            // 分发到指定的业务处理类进行处理该请求
            router.handler?.dispatch { method, instance ->
                runBlocking {
                    val result = method.invoke(instance, params, continuation)
                    if (result == null || result !is HttpResult) {
                        throw IllegalStateException("router dispatch error.")
                    }
                    // todo Convert text to your custom entity and use gson.toJson(entity).
                    val json: String = result.text ?: ""
                    call.respondText(
                        json, ContentType.Application.Json, result.statusCode
                    )
                }
            } ?: call.respondText(
                HttpCodes.getMessage(HttpStatusCode.InternalServerError),
                ContentType.Application.Json,
                HttpStatusCode.InternalServerError
            )
        }
    }
}