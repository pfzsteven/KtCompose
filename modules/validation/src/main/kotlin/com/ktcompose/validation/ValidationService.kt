package com.ktcompose.validation

import com.ktcompose.email.EmailCodeSender
import com.ktcompose.email.MailPlatformManager
import com.ktcompose.email.entity.EmailCode
import com.ktcompose.email.error.EmailError
import com.ktcompose.framework.utils.GsonUtils
import com.ktcompose.framework.utils.LogUtils

/**
 * 对外公开的统一验证服务类
 * Unified verification service class exposed to the outside world
 */
object ValidationService {

    fun init() {
        LogUtils.d(ValidationService::class.java, "ValidationService::init")
        MailPlatformManager.init()
    }

    /**
     * If the code is valid, then return this code. Otherwise, return null
     */
    fun decodeCode(code: String, type: ValidationType): String? {
        return when (type) {
            ValidationType.VALIDATION_BY_EMAIL_CODE -> {
                val now = System.currentTimeMillis()
                val emailCode: EmailCode = GsonUtils.fromJson(code)
                if (emailCode.e >= now) {
                    emailCode.c
                } else {
                    null
                }
            }

            else -> null
        }
    }

    /**
     * 发送验证码进行验证
     * Send verification code for verification
     * @param receiver 邮箱或手机号 Email or mobile number
     */
    suspend fun sendCode(receiver: String, type: ValidationType): Boolean {
        return when (type) {
            ValidationType.VALIDATION_BY_EMAIL_CODE -> {
                val result: EmailError = EmailCodeSender.send(receiver)
                println("send sms code result=$result")
                result == EmailError.SUCCESS
            }

            else -> {
                true
            }
        }
    }

}