package com.ktcompose.router

import org.w3c.dom.Node
import java.util.concurrent.ConcurrentHashMap

/**
 * Parse from the xml file.
 */
object RouterXmlParser {

    @JvmStatic
    private fun parseRouteParams(child: Node, paramsList: ArrayList<RouterParams>?) {
        if (child.hasChildNodes() && paramsList != null) {
            val namesNodeList = child.childNodes
            for (n in 0 until namesNodeList.length) {
                val nameNode = namesNodeList.item(n)
                when (nameNode.nodeName) {
                    "name" -> {
                        val params = RouterParams()
                        paramsList.add(params)
                        nameNode.textContent?.takeIf { it.isNotEmpty() }?.let { nodeValue ->
                            params.name = nodeValue
                            nameNode.attributes?.takeIf { it.length > 0 }?.let { attributes ->
                                for (j in 0 until attributes.length) {
                                    val attr = attributes.item(j)
                                    val value = attr.nodeValue
                                    value.takeIf { it.isNotEmpty() }?.let { v ->
                                        when (attr.nodeName) {
                                            "canBeNull" -> {
                                                params.canBeNull = v.toBoolean()
                                            }

                                            "type" -> {
                                                params.type = v
                                            }

                                            else -> {
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    private fun getApiVersion(node: Node): String? {
        node.attributes?.takeIf { it.length > 0 }?.let { attributes ->
            for (i in 0 until attributes.length) {
                val attr = attributes.item(i)
                val value = attr.textContent
                when (attr.nodeName) {
                    "name" -> {
                        return value
                    }
                }
            }
        }
        return null
    }

    @JvmStatic
    private fun parseRouteAttributes(parent: Node, router: Router, version: String? = "v1") {
        parent.attributes?.takeIf { it.length > 0 }?.let { attributes ->
            for (i in 0 until attributes.length) {
                val attr = attributes.item(i)
                val value = attr.textContent
                when (attr.nodeName) {
                    "path" -> {
                        router.path = if (value.startsWith("/")) {
                            "/${version}${value}"
                        } else {
                            "/${version}/${value}"
                        }
                    }

                    "method" -> {
                        router.method = value
                    }

                    "authorize" -> {
                        router.needAuthorization = value == "true"
                    }
                }
            }
        }
    }

    @JvmStatic
    fun parse(
        parent: Node, routerMap: ConcurrentHashMap<String, Router>, version: String? = "v1"
    ): Any? {
        when (parent.nodeName) {
            "version" -> {
                // api 版本号
                val apiVersion = getApiVersion(parent) ?: "v1"
                if (parent.hasChildNodes()) {
                    val childNodes = parent.childNodes
                    childNodes.takeIf { it.length > 0 }?.let { list ->
                        for (i in 0 until list.length) {
                            parse(list.item(i), routerMap, apiVersion)
                        }
                    }
                }
            }

            "group" -> {
                if (parent.hasChildNodes()) {
                    val childNodes = parent.childNodes
                    childNodes.takeIf { it.length > 0 }?.let { list ->
                        for (i in 0 until list.length) {
                            parse(list.item(i), routerMap, version)
                        }
                    }
                }
            }

            "route" -> {
                val router = Router()
                // attributes
                parseRouteAttributes(parent, router, version)
                // cache
                router.path?.takeIf { it.isNotEmpty() }?.let { path ->
                    routerMap[path] = router
                }
                if (parent.hasChildNodes()) {
                    val childNodes = parent.childNodes
                    childNodes.takeIf { it.length > 0 }?.let { list ->
                        for (i in 0 until list.length) {
                            val child = list.item(i)
                            val childNodeName = child.nodeName
                            val childValue = child.textContent
                            router.handler = router.handler ?: RouterDispatchHandler()
                            when (childNodeName) {
                                "class" -> {
                                    router.handler?.apply {
                                        clazz = Class.forName(childValue)
                                    }
                                }

                                "method" -> {
                                    router.handler?.apply {
                                        functionName = childValue
                                    }
                                }

                                "params" -> {
                                    // 解析请求参数
                                    parseRouteParams(child, router.handler?.params)
                                }
                            }
                        }
                    }
                }
                router.handler?.prepare()
                return router
            }
        }
        return null
    }
}