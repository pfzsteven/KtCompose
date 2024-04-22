package com.ktcompose.router

import com.ktcompose.framework.http.HttpRequest
import com.ktcompose.framework.http.HttpRequestParameters
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
}

data class MethodParameter(val parameterName: String, val type: Class<*>)

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
    private val methodParameters: ArrayList<MethodParameter> by lazy { ArrayList() }

    /**
     * 预初始化，减少在请求过程中动态反射开销
     */
    fun prepare() {
        if (_method == null) {
            clazz?.takeIf { !functionName.isNullOrEmpty() }?.let { clz ->
                clz.declaredMethods.forEach each1@{ method ->
                    method.isAccessible = true
                    if (_method == null) {
                        if (method.name == functionName!! && method.returnType == Any::class.java) {
                            var same = true
                            var i = 0
                            var j = 0
                            while (i < params.size && j < method.parameterCount) {
                                val methodParameterType = method.parameters[j].type
                                if (methodParameterType == HttpRequest::class.java) {
                                    j++
                                    continue
                                }
                                val p = params[i]
                                val xmlDeclaredParamName = p.name
                                val xmlDeclaredParamType = p.type
                                val methodParameterTypeName = methodParameterType.name

                                if (xmlDeclaredParamName.isNullOrEmpty() || !xmlDeclaredParamType.equals(
                                        methodParameterTypeName
                                    )
                                ) {
                                    same = false
                                } else {
                                    methodParameters.add(
                                        MethodParameter(
                                            xmlDeclaredParamName, methodParameterType
                                        )
                                    )
                                }
                                i++
                                j++
                            }
                            if (same) {
                                _method = method
                            }
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
        httpRequestParameters: HttpRequestParameters,
        invoke: suspend (method: Method, instance: Any, args: LinkedList<Any>) -> Unit
    ) {
        val args: LinkedList<Any> = LinkedList()
        for (i in 0 until methodParameters.size) {
            val methodParams = methodParameters[i]
            val methodParameterType = methodParams.type
            val paramValue = httpRequestParameters[methodParams.parameterName]?.toString() ?: continue
            if (methodParameterType == Boolean::class.java) {
                if (paramValue.isNotEmpty()) {
                    args.addLast(paramValue.toBoolean())
                }
            } else if (methodParameterType == Char::class.java) {
                if (paramValue.isNotEmpty()) {
                    args.addLast(paramValue.toCharArray().first());
                }
            } else if (methodParameterType == Byte::class.java) {
                if (paramValue.isNotEmpty()) {
                    args.addLast(paramValue.toInt().toByte());
                }
            } else if (methodParameterType == Float::class.java) {
                if (paramValue.isNotEmpty()) {
                    args.addLast(paramValue.toFloat());
                }
            } else if (methodParameterType == Long::class.java) {
                if (paramValue.isNotEmpty()) {
                    args.addLast(paramValue.toLong());
                }
            } else if (methodParameterType == Double::class.java) {
                if (paramValue.isNotEmpty()) {
                    args.addLast(paramValue.toDouble());
                }
            } else if (methodParameterType == String::class.java) {
                args.addLast(paramValue);
            }
        }
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
}