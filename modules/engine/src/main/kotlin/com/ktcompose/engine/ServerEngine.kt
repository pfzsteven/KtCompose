package com.ktcompose.engine

import com.ktcompose.framework.api.ApiCodesParser
import com.ktcompose.framework.dao.DaoFactory
import com.ktcompose.framework.env.ServerEnvManager
import com.ktcompose.framework.http.HttpUtils
import com.ktcompose.framework.jwt.JwtManager
import com.ktcompose.framework.utils.LogUtils
import com.ktcompose.framework.utils.ResourcesUtils
import com.ktcompose.router.KtorExt.configureHTTP
import com.ktcompose.router.RouterHandler
import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import java.io.File
import java.security.KeyStore
import java.util.*

object ServerEngine {

    /**
     * 短信服务
     */
    @JvmStatic
    private fun initValidationModule() {
        try {
            // 登录/注册验证码服务 login or register validation code service.
            val clz = Class.forName("com.ktcompose.validation.ValidationService")
            clz.getDeclaredMethod("init").apply { isAccessible = true }.let { m ->
                val instance = try {
                    clz.getDeclaredField("INSTANCE").apply {
                        isAccessible = true
                    }.get(null)
                } catch (e: NoSuchFieldException) {
                    clz.getDeclaredConstructor().newInstance()
                }
                m.invoke(instance)
            }
        } catch (ignore: Throwable) {
        }
    }

    @JvmStatic
    private fun init(): Properties {
        // 加载配置 load the 'resources/config.properties' file
        val properties = ResourcesUtils.loadProperties("config.properties")
        // log
        LogUtils.init(properties.getProperty("log.enable"))
        // http
        HttpUtils.init(properties)
        // [可选]若有添加验证模块，则初始化
        initValidationModule()
        // API环境管理 api environment manager
        ServerEnvManager.init(properties.getProperty("api.env", ""))
        // Router
        RouterHandler.init()
        // api 错误码
        ApiCodesParser.init("api_codes.xml")
        // jwt 解析
        JwtManager.initJwtConfig(properties)
        // database
        DaoFactory.init(properties)
        return properties
    }

    /**
     * 统一启动入口
     * @param builder 构建环境
     * @param init 外部可继续初始化application环境
     */
    @JvmStatic
    fun start(builder: ApplicationEngineEnvironmentBuilder, init: (application: Application) -> Unit = {}) {
        val properties = init()
        val keyStoreFile = File("build/keystore.jks")
        val keyStore: KeyStore = if (!keyStoreFile.exists()) {
            buildKeyStore {
                certificate(properties.getProperty("ssl.keystore.alias")) {
                    password = properties.getProperty("ssl.cert.password")
                    domains = listOf("0.0.0.0")
                }
            }.apply {
                saveToFile(keyStoreFile, properties.getProperty("ssl.keystore.password"))
            }
        } else {
            KeyStore.getInstance(keyStoreFile, properties.getProperty("ssl.keystore.password").toCharArray())
        }
        builder.apply {
            connector {
                port = properties.getProperty("http.port", "8080").toInt()
            }
            sslConnector(keyStore = keyStore,
                keyAlias = properties.getProperty("ssl.keystore.alias"),
                keyStorePassword = { properties.getProperty("ssl.keystore.password").toCharArray() },
                privateKeyPassword = { properties.getProperty("ssl.cert.password").toCharArray() }) {
                port = properties.getProperty("https.port", "8443").toInt()
                keyStorePath = keyStoreFile
            }
            module {
                configureHTTP()
                configureRouting()
                init(this)
            }
        }
    }

    @JvmStatic
    private fun Application.configureRouting() {
        routing {
            RouterHandler.filter(HttpMethod.Get) { path, router ->
                get(path) {
                    RouterHandler.handle(call, router)
                }
            }
            RouterHandler.filter(HttpMethod.Post) { path, router ->
                post(path) {
                    RouterHandler.handle(call, router)
                }
            }
        }
    }
}