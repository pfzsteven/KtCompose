package com.ktcompose.router

import com.ktcompose.framework.http.HttpRequestParameters
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * 客户端请求某个path所需参数
 */
class RouterParams {
    /**
     * 变量名
     */
    var name: String? = ""

    /**
     * 是否支持为空(不传)
     */
    var canBeNull: Boolean = true
}

/**
 * 路由分发处理逻辑的 类+函数
 */
class RouterDispatchHandler {
    /**
     * 类
     */
    var clazz: Class<*>? = null

    /**
     * 函数名
     */
    var functionName: String? = null

    private var _method: Method? = null
    private var _instance: Any? = null

    /**
     * 分发执行
     */
    suspend fun dispatch(invoke: suspend (method: Method, instance: Any) -> Unit) {
        if (_method == null) {
            clazz?.takeIf { !functionName.isNullOrEmpty() }?.let { clz ->
                clz.getDeclaredMethod(
                    functionName!!, HttpRequestParameters::class.java, Continuation::class.java
                ).apply { isAccessible = true }.let { m ->
                    _method = m
                }
                if (_instance == null) {
                    _instance = try {
                        clz.getDeclaredField("INSTANCE").apply {
                            isAccessible = true
                        }.get(null)
                    } catch (e: NoSuchFieldException) {
                        clz.getDeclaredConstructor().newInstance()
                    }
                }
            }
        }
        _method?.takeIf { _instance != null }?.let { m ->
            invoke(m, _instance!!)
        }
    }
}

/**
 * 路由类
 */
class Router {

    /**
     * 对外暴露的path
     */
    var path: String? = ""

    /**
     * 请求方法 get/post/put/delete等
     */
    var method: String? = "get"

    /**
     * 是否需要鉴权
     */
    var needAuthorization: Boolean = false

    /**
     * 该路由携带的请求参数集合
     */
    var params: ArrayList<RouterParams>? = null

    /**
     * 分发该路由进行处理
     */
    var handler: RouterDispatchHandler? = null
}