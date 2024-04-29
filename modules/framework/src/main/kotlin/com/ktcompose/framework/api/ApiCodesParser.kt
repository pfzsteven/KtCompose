package com.ktcompose.framework.api

import com.ktcompose.framework.http.HttpCodes
import com.ktcompose.framework.utils.LogUtils
import com.ktcompose.framework.utils.ResourcesUtils
import com.ktcompose.framework.utils.XmlUtils
import io.ktor.http.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 解析错误码配置
 */
object ApiCodesParser {

    val codesMap: ConcurrentHashMap<Int, ApiCodes> by lazy { ConcurrentHashMap<Int, ApiCodes>() }

    @JvmStatic
    fun init(resName: String) {
        ApiCodes(HttpStatusCode.OK.value).apply {
            codesMap[code] = this
        }
        ApiCodes(HttpStatusCode.InternalServerError.value).apply {
            codesMap[code] = this
        }
        ApiCodes(HttpStatusCode.NotFound.value).apply {
            codesMap[code] = this
        }
        ResourcesUtils.loadResourceFile(resName)?.let { stream ->
            XmlUtils.parse(stream) { childNode ->
                when (childNode.nodeName) {
                    "Code" -> {
                        var code: Int? = null
                        childNode.attributes?.takeIf { it.length > 0 }?.let { attrs ->
                            attrs.getNamedItem("value")?.takeIf { !it.textContent.isNullOrEmpty() }?.let { value ->
                                try {
                                    code = value.textContent.toInt()
                                } catch (e: Throwable) {
                                    LogUtils.e(HttpCodes::class.java, e)
                                }
                            }
                        }
                        code?.let { c ->
                            var zh: String? = null
                            var tw: String? = null
                            var en: String? = null
                            if (childNode.hasChildNodes()) {
                                val messageNodes = childNode.childNodes
                                for (j in 0 until messageNodes.length) {
                                    val messageNode = messageNodes.item(j)
                                    val message = messageNode.textContent ?: ""
                                    when (messageNode.nodeName) {
                                        "zh" -> {
                                            zh = message
                                        }

                                        "tw" -> {
                                            tw = message
                                        }

                                        "en" -> {
                                            en = message
                                        }
                                    }
                                }
                            }
                            ApiCodes(c, zh, tw, en).apply {
                                codesMap[c] = this
                            }
                        }
                    }
                }
            }
        }
    }
}