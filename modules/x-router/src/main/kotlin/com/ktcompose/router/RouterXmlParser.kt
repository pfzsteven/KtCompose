package com.ktcompose.router

import org.w3c.dom.Node
import java.util.concurrent.ConcurrentHashMap

/**
 * Parse from the xml file.
 */
object RouterXmlParser {

    @JvmStatic
    private fun parseRouteParams(child: Node, paramsList: ArrayList<RouterParams>) {
        if (child.hasChildNodes()) {
            val params = RouterParams()
            paramsList.add(params)
            val namesNodeList = child.childNodes
            for (n in 0 until namesNodeList.length) {
                val nameNode = namesNodeList.item(n)
                when (nameNode.nodeName) {
                    "name" -> {
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
    private fun parseVersionAttributes(node: Node): String? {
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

                    "authorization" -> {
                        router.needAuthorization = value == "required"
                    }
                }
            }
        }
    }

    @JvmStatic
    fun parse(parent: Node, routerMap: ConcurrentHashMap<String, Router>, version: String? = "v1"): Any? {
        when (parent.nodeName) {
            "version" -> {
                // api 版本号
                parseVersionAttributes(parent)?.let { apiVersion ->
                    if (parent.hasChildNodes()) {
                        val childNodes = parent.childNodes
                        childNodes.takeIf { it.length > 0 }?.let { list ->
                            for (i in 0 until list.length) {
                                parse(list.item(i), routerMap, version)
                            }
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
                parseRouteAttributes(parent, router)
                // cache
                router.path?.takeIf { it.isNotEmpty() }?.let { path ->
                    routerMap[path] = router
                }
                if (parent.hasChildNodes()) {
                    val childNodes = parent.childNodes
                    val paramsList = ArrayList<RouterParams>()
                    router.params = paramsList
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
                                    parseRouteParams(child, paramsList)
                                }
                            }
                        }
                    }
                }
                return router
            }

            else -> {
            }
        }
        return null
    }
}