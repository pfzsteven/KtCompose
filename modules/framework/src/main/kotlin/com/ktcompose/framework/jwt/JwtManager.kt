package com.ktcompose.framework.jwt

import com.ktcompose.framework.utils.LogUtils
import io.ktor.http.*
import java.util.*

/**
 * jwt 管理
 */
object JwtManager {

    private lateinit var jwtVerifier: ThreadLocal<IJwtVerify>
    private lateinit var jwtConfig: JwtConfig

    /**
     * 初始化jwt配置
     */
    fun initJwtConfig(properties: Properties) {
        jwtConfig = JwtConfig(
            properties.getProperty("jwt.hs256.secret"),
            properties.getProperty("jwt.rs256.kid", ""),
            properties.getProperty("jwt.rs256.private_key", ""),
            properties.getProperty("jwt.issuer"),
            properties.getProperty("jwt.audience"),
            properties.getProperty("jwt.auth_key", HttpHeaders.Authorization),
            properties.getProperty("jwt.expired_days", "")
        )
        var verifier: Class<*>? = properties.getProperty("")?.let { textContent ->
            // 检查是否实现了IJwtVerify 接口
            Class.forName(textContent).takeIf { it.isAssignableFrom(IJwtVerify::class.java) }
        }
        verifier = verifier ?: DefaultJwtVerifier::class.java
        jwtVerifier = ThreadLocal.withInitial {
            verifier.let { clz ->
                val instance = try {
                    clz.getDeclaredField("INSTANCE").apply {
                        isAccessible = true
                    }.get(null)
                } catch (e: NoSuchFieldException) {
                    clz.getDeclaredConstructor().newInstance()
                }
                instance as IJwtVerify
            }
        }
    }

    /**
     * Http Header
     */
    fun tokenName() = jwtConfig.authKey

    /**
     * 生成jwt值
     */
    suspend fun generateJwt(): String {
        if (LogUtils.enable()) {
            LogUtils.d(JwtManager::class.java, "generateJwt ...")
        }
        val map: HashMap<String, String> = HashMap()
        jwtVerifier.get().buildPayload(map)
        return jwtConfig.generateJwt(map)
    }

    suspend fun verify(authentication: String?): Boolean {
        if (authentication.isNullOrEmpty()) return false
        val pass = authentication.split('.').takeIf { it.size == 3 }?.let { parts ->
            val payload = parts[1]
            if (!jwtConfig.checkIsExpired(payload)) {
                false
            } else {
                val header = parts[0]
                val signature = parts[2]
                // 判断签名是否正确
                val newSignature = jwtConfig.sign(header, payload)
                if (signature == newSignature) {
                    // 没有过期且自定义检查通过
                    jwtVerifier.get().verify(authentication, payload)
                } else {
                    // 签名验证不通过
                    false
                }
            }
        } ?: false
        return pass
    }

}