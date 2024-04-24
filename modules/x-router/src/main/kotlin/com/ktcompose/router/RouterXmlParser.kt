package com.ktcompose.router

import com.ktcompose.framework.role.Role
import org.w3c.dom.Node
import java.util.concurrent.ConcurrentHashMap

/**
 * Parse from the xml file.
 */
object RouterXmlParser {

    @JvmStatic
    private fun convert2BasicType(type: String): String {
        // int|string|float|double|long|short|byte
        return when (type) {
            "int" -> "java.lang.Integer"
            "float" -> "java.lang.Float"
            "double" -> "java.lang.Double"
            "long" -> "java.lang.Long"
            "short" -> "java.lang.Short"
            "byte" -> "java.lang.Byte"
            else -> "java.lang.String"
        }
    }

    /**
     * admin|developer|operator|user|guest
     */
    @JvmStatic
    private fun convert2RoleLevel(value: String): Role {
        return when (value) {
            "developer" -> Role.Developer
            "operator" -> Role.Operator
            "user" -> Role.User
            "guest" -> Role.Guest
            else -> Role.Admin
        }
    }

    @JvmStatic
    private fun parsePermission(node: Node): HashSet<Role> {
        val levels = HashSet<Role>()
        if (node.hasChildNodes()) {
            val childNodes = node.childNodes
            for (i in 0 until childNodes.length) {
                val child = childNodes.item(i)
                if (child.nodeName === "role") {
                    val attrs = child.attributes
                    for (j in 0 until attrs.length) {
                        val attr = attrs.item(j)
                        when (val attrValue = attr.nodeValue) {
                            "all" -> {
                                levels.addAll(Role.entries.toTypedArray())
                            }

                            else -> {
                                levels.add(convert2RoleLevel(attrValue))
                            }
                        }
                    }
                }
            }
        } else {
            levels.addAll(Role.entries.toTypedArray())
        }
        return levels
    }

    @JvmStatic
    private fun parseRouteParams(child: Node, paramsList: ArrayList<RouterParams>?) {
        if (child.hasChildNodes() && paramsList != null) {
            val namesNodeList = child.childNodes
            for (n in 0 until namesNodeList.length) {
                val nameNode = namesNodeList.item(n)
                when (nameNode.nodeName) {
                    "param" -> {
                        val params = RouterParams()
                        paramsList.add(params)
                        nameNode.attributes?.takeIf { it.length > 0 }?.let { attributes ->
                            for (j in 0 until attributes.length) {
                                val attr = attributes.item(j)
                                val value = attr.nodeValue
                                value.takeIf { it.isNotEmpty() }?.let { v ->
                                    when (attr.nodeName) {
                                        "name" -> {
                                            params.name = v
                                        }

                                        "canBeNull" -> {
                                            params.canBeNull = v.toBoolean()
                                        }

                                        "type" -> {
                                            params.type = convert2BasicType(v)
                                        }

                                        "default" -> {
                                            params.defaultValue = v
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
                                "permission" -> {
                                    router.permission = parsePermission(child)
                                }

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