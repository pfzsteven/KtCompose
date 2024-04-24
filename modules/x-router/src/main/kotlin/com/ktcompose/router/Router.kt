package com.ktcompose.router

import com.ktcompose.framework.http.HttpRequest
import com.ktcompose.framework.role.Role
import java.lang.reflect.Method
import java.util.*

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

    /**
     * 类型
     */
    var type: String? = null

    /**
     * 默认值
     */
    var defaultValue: String? = null
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

    /**
     * 该路由携带的请求参数集合
     */
    var params: ArrayList<RouterParams> = ArrayList()

    private var _method: Method? = null
    private var _instance: Any? = null

    fun prepare() {
        if (_method == null) {
            clazz?.takeIf { !functionName.isNullOrEmpty() }?.let { clz ->
                clz.declaredMethods.forEach each1@{ method ->
                    method.isAccessible = true
                    if (_method == null) {
                        if (method.name == functionName!! && method.returnType == Any::class.java) {
                            var i = 0
                            var j = 0
                            while (i < params.size && j < method.parameterCount) {
                                val methodParameterType = method.parameters[j].type
                                if (methodParameterType == HttpRequest::class.java) {
                                    _method = method
                                    break
                                }
                                i++
                                j++
                            }
                        }
                        if (_method != null) {
                            return@each1
                        }
                    } else {
                        return@each1
                    }
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
    }

    /**
     * 分发执行
     */
    suspend fun dispatch(
        invoke: suspend (method: Method, instance: Any, args: LinkedList<Any>) -> Unit
    ) {
        val args: LinkedList<Any> = LinkedList()
        _method?.takeIf { _instance != null }?.let { m ->
            invoke(m, _instance!!, args)
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
     * 分发该路由进行处理
     */
    var handler: RouterDispatchHandler? = null

    /**
     * 访问路由权限
     */
    lateinit var permission: HashSet<Role>
}