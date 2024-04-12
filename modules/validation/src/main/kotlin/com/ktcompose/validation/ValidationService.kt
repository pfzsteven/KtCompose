package com.ktcompose.validation

import com.ktcompose.email.EmailCodeSender
import com.ktcompose.email.MailPlatformManager
import com.ktcompose.email.error.EmailError
import com.ktcompose.framework.http.AutoInvoke
import com.ktcompose.framework.utils.LogUtils

/**
 * 对外公开的统一验证服务类
 * Unified verification service class exposed to the outside world
 */
object ValidationService {

    @AutoInvoke
    fun init() {
        LogUtils.d(ValidationService::class.java, "ValidationService::init")
        MailPlatformManager.init()
    }

    /**
     * 发送验证码进行验证
     * Send verification code for verification
     * @param receiver 邮箱或手机号 Email or mobile number
     */
    @JvmStatic
    fun sendCode(receiver: String, type: ValidationType) {
        when (type) {
            ValidationType.VALIDATION_BY_EMAIL_CODE -> {
                val result: EmailError = EmailCodeSender.send(receiver)
                println("send result=$result")
            }

            else -> {
            }
        }
    }

}