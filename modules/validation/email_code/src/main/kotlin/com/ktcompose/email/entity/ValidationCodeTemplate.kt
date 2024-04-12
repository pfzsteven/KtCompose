package com.ktcompose.email.entity

import java.io.Serializable

/**
 * 验证码模板
 * Verification code template
 */
class ValidationCodeTemplate : Serializable {
    var subject: String? = null
    var body: String? = null

    override fun toString(): String {
        return "$subject \n $body"
    }
}