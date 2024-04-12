package com.ktcompose.email.entity

import java.io.Serializable

/**
 * 邮箱平台配置信息
 * Email platform configuration information
 */
class EmailPlatform : Serializable {
    var account: String? = null
    var host: String? = null
    var port: Int = 0
    var user: String? = null
    var pwd: String? = null
}
