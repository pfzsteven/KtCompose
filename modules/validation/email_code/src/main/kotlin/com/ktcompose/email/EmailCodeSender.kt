package com.ktcompose.email

import com.ktcompose.email.entity.ValidationCodeTemplate
import com.ktcompose.email.error.EmailError
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
    @Synchronized
    @JvmStatic
    fun send(email: String): EmailError {
        if (!emailRegex.matches(email)) {
            return EmailError.ERR_EMAIL_TYPE
        }
        val code: String =
            template.body?.takeIf { it.isNotEmpty() }?.replace("{code}", EmailCodeGenerator.generateCode(CODE_LENGTH))
                ?: ""
        if (code.isEmpty()) {
            return EmailError.ERR_CONTENT
        }
        val success = MailPlatformManager.getPlatform()?.send(email, template.subject ?: "", code) ?: false
        return if (success) EmailError.SUCCESS else EmailError.ERR_UNKNOWN
    }
}