package com.ktcompose.email.error

enum class EmailError {
    SUCCESS,
    ERR_UNKNOWN,

    /**
     * 邮件格式错误 Email format error
     */
    ERR_EMAIL_TYPE,

    /**
     * 邮件内容错误 Email content error
     */
    ERR_CONTENT
}