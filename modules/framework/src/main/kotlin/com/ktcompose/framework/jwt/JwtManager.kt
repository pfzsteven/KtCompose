package com.ktcompose.framework.jwt

import com.ktcompose.framework.role.RoleConstants
import com.ktcompose.framework.role.Role
import com.ktcompose.framework.utils.GsonUtils
import io.ktor.http.*
import java.util.*

/**
 * jwt
 */
object JwtManager {

    private lateinit var jwtVerifier: ThreadLocal<IJwtVerify>
    private lateinit var jwtConfig: JwtConfig

    /**
     * init
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
    fun httpHeaderName() = jwtConfig.authKey

    /**
     * generate jwt text
     * @param role save role value in token
     * @param block add more unique key-values
     */
    suspend fun generateJwt(role: Role, block: (map: HashMap<String, String>) -> Unit): String {
        val map: HashMap<String, String> = HashMap()
        block(map)
        jwtVerifier.get().buildPayload(map)
        map[RoleConstants.JWT_KEY_ROLE] = role.level.toString()
        return jwtConfig.generateJwt(map)
    }

    private fun decodePayload(authentication: String): JwtPayload? {
        return authentication.split('.').takeIf { it.size == 3 }?.let { parts ->
            GsonUtils.fromJson<JwtPayload>(parts[1])
        }
    }

    fun verifyPermission(authentication: String?, requirePermissions: HashSet<Role>): Boolean {
        return if (authentication.isNullOrEmpty()) {
            requirePermissions.contains(Role.Guest)
        } else {
            decodePayload(authentication)?.let { jwtPayload ->
                jwtPayload.role()?.let { currentRole ->
                    if (currentRole != Role.Admin) {
                        requirePermissions.contains(currentRole)
                    } else {
                        true
                    }
                } ?: false
            } ?: false
        }
    }

    /**
     * verify jwt token is invalid or not.
     * @param path api url path
     * @param authentication token
     */
    suspend fun verify(path: String, authentication: String?): Boolean {
        if (authentication.isNullOrEmpty()) return false
        val pass = authentication.split('.').takeIf { it.size == 3 }?.let { parts ->
            val payload = parts[1]
            if (!jwtConfig.checkIsExpired(payload)) {
                false
            } else {
                val header = parts[0]
                val signature = parts[2]
                val newSignature = jwtConfig.sign(header, payload)
                if (signature == newSignature) {
                    jwtVerifier.get().verify(path, authentication, GsonUtils.fromJson<JwtPayload>(payload))
                } else {
                    false
                }
            }
        } ?: false
        return pass
    }

}