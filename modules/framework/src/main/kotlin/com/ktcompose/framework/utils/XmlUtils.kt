package com.ktcompose.framework.utils

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

object XmlUtils {

    @JvmStatic
    inline fun parse(inputStream: InputStream, parser: (node: Node) -> Unit) {
        inputStream.use {
            val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val doc: Document = factory.newDocumentBuilder().parse(it)
            doc.documentElement.childNodes.let { nodeList ->
                for (i in 0 until nodeList.length) {
                    val node = nodeList.item(i)
                    parser(node)
                }
            }
        }
    }

    @JvmStatic
    inline fun <reified T : Any> parse(inputStream: InputStream): T {
        val instance: T = T::class.java.getDeclaredConstructor().newInstance()
        inputStream.use {
            val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val doc: Document = factory.newDocumentBuilder().parse(it)
            doc.documentElement.childNodes.let { nodeList ->
                for (i in 0 until nodeList.length) {
                    val node = nodeList.item(i)
                    val nodeName = node.nodeName
                    if (nodeName.startsWith("#")) {
                        continue
                    }
                    var text = node.textContent
                    if (text.first() == '\n') {
                        text = text.substring(1)
                    }
                    if (text.last() == '\n') {
                        text = text.substring(0, text.length - 1)
                    }
                    try {
                        val field = instance.javaClass.getDeclaredField(nodeName)
                        field.isAccessible = true
                        field.set(instance, text)
                    } catch (e: Exception) {
                        LogUtils.w(XmlUtils::class.java, e)
                    }
                }
            }
        }
        return instance
    }
}