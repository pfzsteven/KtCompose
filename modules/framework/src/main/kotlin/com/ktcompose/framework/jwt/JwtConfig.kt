package com.ktcompose.framework.jwt

import com.auth0.jwt.algorithms.Algorithm
import com.ktcompose.framework.utils.GsonUtils
import org.json.JSONObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.max

/**
 * Header.Payload.Signature
 */
data class JwtConfig(
    val hs256Secret: String?,
    val rs256Kid: String?,
    val rs256PrivateKey: String?,
    val issuer: String,
    val audience: String,
    val authKey: String,
    private val expiredDays: String
) {

    private fun isHS256(): Boolean {
        return !hs256Secret.isNullOrEmpty() && rs256Kid.isNullOrEmpty() && rs256PrivateKey.isNullOrEmpty()
    }

    /**
     * jwt-header部分
     */
    @OptIn(ExperimentalEncodingApi::class)
    private fun buildJwtHeader(): String {
        return Base64.encode(JSONObject().apply {
            put("typ", "JWT")
            put(
                "alg", if (isHS256()) {
                    "HS256"
                } else {
                    "RS256"
                }
            )
        }.toString().encodeToByteArray())
    }

    /**
     * iss (issuer)：签发人
     * exp (expiration time)：过期时间
     * sub (subject)：主题
     * aud (audience)：受众
     * nbf (Not Before)：生效时间
     * iat (Issued At)：签发时间
     * jti (JWT ID)：编号
     */
    @OptIn(ExperimentalEncodingApi::class)
    private fun buildJwtPayload(extraPayloadData: HashMap<String, String>): String {
        return Base64.Default.encode(JSONObject().apply {
            put("iss", issuer)
            put("exp", expiredTime())
            put("aud", audience)
            put("iat", System.currentTimeMillis())
            extraPayloadData.forEach { (k, v) ->
                put(k, v)
            }
        }.toString().encodeToByteArray())
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decodeHeader(header: String): String {
        return Base64.decode(header.toByteArray()).decodeToString()
    }

    /**
     * 解析数据部分
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun checkIsExpired(payload: String): Boolean {
        val jsonStr = Base64.decode(payload.toByteArray()).decodeToString()
        if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
            val pl = GsonUtils.fromJson<JSONObject>(jsonStr)
            val current = System.currentTimeMillis()
            val exp = pl.optLong("exp", current + 1000L)
            return exp < current
        }
        return true
    }

    /**
     * 签名，生成 signature 字符串
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun sign(header: String, payload: String): String {
        val signature: ByteArray = if (isHS256()) {
            Algorithm.HMAC256(hs256Secret!!).sign(header.toByteArray(), payload.toByteArray())
        } else {
            ByteArray(0)
        }
        return Base64.encode(signature)
    }

    /**
     * 生成jwt
     */
    fun generateJwt(extraPayloadData: HashMap<String, String>): String {
        val header = buildJwtHeader()
        val payload = buildJwtPayload(extraPayloadData)
        val signature = sign(header, payload)
        return "${header}.${payload}.${signature}"
    }

    /**
     * 过期时间
     */
    private fun expiredTime(): Long {
        return System.currentTimeMillis() + (if (expiredDays.isEmpty()) {
            Int.MAX_VALUE
        } else {
            max(1, expiredDays.toInt()) * 3600000
        })
    }


}