package com.ktcompose.email

import com.ktcompose.email.entity.EmailCode
import com.ktcompose.email.entity.ValidationCodeTemplate
import com.ktcompose.email.error.EmailError
import com.ktcompose.framework.redis.RedisManager
import com.ktcompose.framework.utils.GsonUtils
import com.ktcompose.framework.utils.LogUtils
import com.ktcompose.framework.utils.ResourcesUtils
import com.ktcompose.framework.utils.XmlUtils

object EmailCodeSender {

    /**
     * 邮箱格式合法性判断
     * Determining the legality of email formats
     */
    private val emailRegex: Regex by lazy { Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+$") }
    private const val CODE_LENGTH = 6
    private val template: ValidationCodeTemplate by lazy {
        ResourcesUtils.loadResourceFile("validation_code_template.xml")?.let {
            XmlUtils.parse<ValidationCodeTemplate>(it)
        } ?: ValidationCodeTemplate()
    }

    /**
     * @param email 对方邮箱 The other party's email address
     */
    suspend fun send(email: String): EmailError {
        LogUtils.i(EmailCodeSender::class.java, "Send code to email:$email")
        if (!emailRegex.matches(email)) {
            return EmailError.ERR_EMAIL_TYPE
        }
        val code = EmailCodeGenerator.generateCode(CODE_LENGTH)
        val text: String = template.body?.takeIf { it.isNotEmpty() }?.replace("{code}", code) ?: ""
        if (text.isEmpty()) {
            return EmailError.ERR_CONTENT
        }
        val success = MailPlatformManager.getPlatform()?.send(email, template.subject ?: "", text) ?: false
        return if (success) {
            // save code to redis server.
            RedisManager.set(email, GsonUtils.gson.toJson(EmailCode(code)))
            EmailError.SUCCESS
        } else {
            EmailError.ERR_UNKNOWN
        }
    }
}