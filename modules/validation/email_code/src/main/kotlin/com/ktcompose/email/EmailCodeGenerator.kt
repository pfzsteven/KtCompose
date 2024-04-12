package com.ktcompose.email

import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * 邮箱验证码生成器
 * Email verification code generator
 */
internal object EmailCodeGenerator {

    private val random: Random by lazy { Random() }
    private val counter: AtomicLong by lazy { AtomicLong() }

    /**
     * 生成code范围
     * Generate code range
     */
    private const val NUM_STR: String = "0123456789"

    /**
     * 默认生成code长度
     * Default generated code length
     */
    private const val DEFAULT_CODE_LENGTH = 6

    @JvmStatic
    @Synchronized
    fun generateCode(length: Int = DEFAULT_CODE_LENGTH): String {
        val sb = StringBuilder(length)
        val count = counter.getAndIncrement()
        sb.append(count)
        for (i in 0 until (length - "$count".length)) {
            sb.append(NUM_STR[random.nextInt(NUM_STR.length)])
        }
        return sb.toString()
    }
}