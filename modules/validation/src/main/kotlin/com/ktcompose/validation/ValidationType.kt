package com.ktcompose.validation

/**
 * 验证方式
 * Ways of identifying
 */
enum class ValidationType {
    /**
     * 邮箱验证码
     * E-mail verification code
     */
    VALIDATION_BY_EMAIL_CODE,
    /**
     * 短信验证码
     * SMS verification code
     */
    VALIDATION_BY_SMS_CODE
}