package com.ktcompose.email

import com.ktcompose.email.entity.EmailPlatform
import com.ktcompose.framework.utils.GsonUtils
import com.ktcompose.framework.utils.ResourcesUtils
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.HtmlEmail
import org.json.JSONObject

/**
 * 根据配置选择发送邮件平台
 */
object MailPlatformManager {

    /**
     * 在当前模块的resources目录下创建
     * Created in the resources directory of the current module
     */
    private const val RES_NAME_PUBLIC = "email_conf.json"

    /**
     * 若不会将关键账号信息暴露到git上，则和email_conf.json同级的目录下，创建一份email_conf_secret.json，将会替换解析
     * If key account information will not be exposed to git, create an email_conf_secret.json in the same directory as email_conf.json, which will replace the parsing
     */
    private const val RES_NAME_SECRET = "email_conf_secret.json"
    private var mailPlatform: MailPlatform? = null

    @JvmStatic
    fun getPlatform(): MailPlatform? = mailPlatform

    @JvmStatic
    fun init() {
        // 判断是否本地存在 RES_NAME_SECRET 文件
        // Determine whether the RES_NAME_SECRET file exists locally
        val file: String = if (ResourcesUtils.hasResourceFile(RES_NAME_SECRET)) {
            RES_NAME_SECRET
        } else {
            RES_NAME_PUBLIC
        }
        ResourcesUtils.readResourceAsText(file)?.let {
            val jsonObject = JSONObject(it)
            jsonObject.optString("default_platform")?.let { key ->
                if (jsonObject.has(key)) {
                    val platform: EmailPlatform = GsonUtils.fromJson(jsonObject.getJSONObject(key).toString())
                    mailPlatform = MailPlatform(platform)
                }
            }
        }
    }
}

/**
 * 通用发送邮件入口
 * Universal sending email portal
 */
class MailPlatform(private val data: EmailPlatform) {

    /**
     * 发送邮件
     * send email
     * @param targetEmailAccount 对方邮箱 The other party's email address
     * @param subject 邮件主题 Email Subject
     * @param content 邮件内容 content of email
     */
    fun send(targetEmailAccount: String, subject: String, content: String): Boolean {
        if (content.isEmpty()) {
            return false
        }
        val email = HtmlEmail()
        email.hostName = data.host
        email.setSmtpPort(data.port)
        email.setAuthenticator(DefaultAuthenticator(data.user, data.pwd))
        email.isSSLOnConnect = true
        email.setStartTLSEnabled(true)
        email.setFrom(data.account)
        email.subject = subject
        email.setMsg(content)
        email.addTo(targetEmailAccount)
        try {
            email.send()
            return true
        } catch (t: EmailException) {
            t.printStackTrace()
        }
        return false
    }
}