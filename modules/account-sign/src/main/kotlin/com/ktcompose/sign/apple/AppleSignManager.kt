package com.ktcompose.sign.apple

import com.ktcompose.framework.http.HttpResult
import com.ktcompose.framework.http.HttpUtils
import com.ktcompose.framework.utils.GsonUtils
import com.ktcompose.framework.utils.LogUtils
import io.jsonwebtoken.Jwts
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Base64
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*
import java.util.concurrent.ConcurrentHashMap


data class AppleJwtHeader(val kid: String, val alg: String)

class AppleJwtPayload {
    var iss: String? = null
    var aud: String? = null
    var exp: Long = 0L
    var iat: Long = 0L
    var sub: String? = null
    var c_hash: String? = null
    var email: String? = null
    var email_verified: String? = null
    var auth_time: Long = 0L
    var nonce_supported: Boolean = true
}

class AppleKey {
    var kty: String? = null
    var kid: String? = null
    var use: String? = null
    var alg: String? = null
    var n: String? = null
    var e: String? = null
}

class ApplePublicKeyJson {
    var keys: ArrayList<AppleKey>? = null
}

object AppleSignManager {

    private const val APPLE_PUBLIC_KEY_URL = "https://appleid.apple.com/auth/keys"
    private const val APPLE_ISS = "https://appleid.apple.com"

    private lateinit var audience: String
    private val publicKeys: ConcurrentHashMap<String, RSAPublicKey> by lazy { ConcurrentHashMap() }

    //    private lateinit var appKeyId: String
//    private lateinit var teamId: String
//    private var privateKey: PrivateKey? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun init(properties: Properties) {
        audience = properties.getProperty("apple.audience", "")
//        appKeyId = properties.getProperty("apple.app_key_id")
//        teamId = properties.getProperty("apple.team_id")
//        properties.getProperty("apple.private_keystore")?.takeIf { it.isNotEmpty() }?.apply {
//            ResourcesUtils.loadResourceFile(this)?.let {
//                initPrivateKey(it.readAllBytes())
//            } ?: throw FileNotFoundException("Can't load apple private key store file(xxx.p8)")
//        }
        // preload
        GlobalScope.launch(Dispatchers.IO) {
            getPublicKey()
        }
    }

    /**
     * @return email
     */
    suspend fun verify(identityToken: String): String? {
        var email: String? = null
        identityToken.split('.').takeIf { it.size == 3 }?.let { parts ->
            val headerJson = Base64.decodeBase64(parts[0]).decodeToString()
            val payloadJson = Base64.decodeBase64(parts[1]).decodeToString()
            val header = GsonUtils.fromJson<AppleJwtHeader>(headerJson)
            val payload = GsonUtils.fromJson<AppleJwtPayload>(payloadJson)
            LogUtils.d(LogUtils::class.java, headerJson)
            LogUtils.d(LogUtils::class.java, payloadJson)
            if (payload.aud != audience) {
                LogUtils.e(AppleSignManager::class.java, "verify fail.Audience is error.")
                return null
            }
            getPublicKey()
            publicKeys[header.kid]?.let { key ->
                val jwtParser = Jwts.parser().requireIssuer(APPLE_ISS).requireAudience(audience).verifyWith(key).build()
                jwtParser.parseSignedClaims(identityToken)?.let { claims ->
                    claims.payload.apply {
                        val sub = this["sub"].toString()
                        val iss = this["iss"].toString()
                        val aud = this["aud"].toString()
                        val exp = this["exp"].toString().toLong() * 1000L
                        val em = this["email"]?.toString()
                        val emailVerified: Boolean = this["email_verified"]?.toString()?.toBoolean() == true
                        val pass =
                            APPLE_ISS == iss && System.currentTimeMillis() < exp && emailVerified && em?.equals(payload.email) == true
                        LogUtils.d(
                            AppleSignManager::class.java,
                            "claims.payload: sub=$sub ,iss=$iss, aud=$aud, exp=$exp ,em=$em, pass?$pass"
                        )
                        if (pass) {
                            email = em
                        }
                    }
                }
            }
        }
        return email
    }

    private fun initPrivateKey(p8der: ByteArray) {
//        privateKey = KeyFactory.getInstance("RSA")
//            .generatePrivate(PKCS8EncodedKeySpec(Base64().decode(p8der)))
    }

    /**
     * preload public keys
     */
    private suspend fun getPublicKey() {
        LogUtils.d(AppleSignManager::class.java, "getPublicKey start ....")
        if (publicKeys.isEmpty()) {
            val httpResult: HttpResult = HttpUtils.get(APPLE_PUBLIC_KEY_URL)
            LogUtils.d(AppleSignManager::class.java, "getPublicKey Complete.Http status: ${httpResult.statusCode}")
            if (httpResult.success()) {
                httpResult.text?.takeIf { it.startsWith("{") && it.endsWith("}") }?.apply {
                    val publicKeyJson = GsonUtils.fromJson<ApplePublicKeyJson>(this)
                    publicKeyJson.keys?.forEach { appleKey ->
                        if (appleKey.kid.isNullOrEmpty() || appleKey.n.isNullOrEmpty() || appleKey.e.isNullOrEmpty() || appleKey.alg.isNullOrEmpty()) {
                            return@forEach
                        }
                        val kid = appleKey.kid!!
                        val e = appleKey.e!!
                        val n = appleKey.n!!
                        val eInt = BigInteger(1, Base64.decodeBase64(e))
                        val nInt = BigInteger(1, Base64.decodeBase64(n))
                        val key =
                            KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(nInt, eInt)) as RSAPublicKey
                        publicKeys[kid] = key
                    }
                }
            } else {
                LogUtils.e(AppleSignManager::class.java, "getPublicKey fail.${httpResult.statusCode}")
            }
        }
    }
}