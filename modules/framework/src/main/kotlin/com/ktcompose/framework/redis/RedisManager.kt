package com.ktcompose.framework.redis

import com.ktcompose.framework.utils.LogUtils
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.resource.ClientResources
import io.lettuce.core.resource.DefaultClientResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.*

object RedisManager {

    private var redisClient: RedisClient? = null
    private var redisCommands: RedisAsyncCommands<String, String>? = null

    fun init(properties: Properties) {
        val redisURI =
            RedisURI.Builder.redis(properties.getProperty("redis.host"), properties.getProperty("redis.port").toInt())
                .withAuthentication(
                    properties.getProperty("redis.user", ""), properties.getProperty("redis.pwd", "")
                ).withSsl(properties.getProperty("redis.ssl", "false").toBoolean()).withDatabase(1).withTimeout(
                    Duration.ofSeconds(properties.getProperty("redis.timeout_sec", "60").toLong())
                ).build()
        val clientResources: ClientResources = DefaultClientResources.builder().build()
        redisClient = RedisClient.create(clientResources, redisURI)
        redisCommands = redisClient?.connect()?.async()
    }

    fun get(key: String): String? {
        if (redisCommands == null) {
            return null
        }
        var value: String? = null
        redisCommands!!.get(key).thenAccept {
            value = it
        }.toCompletableFuture().join()
        if (LogUtils.enable()) {
            LogUtils.d(RedisManager::class.java, "Redis get $key => $value")
        }
        return value
    }

    suspend fun set(key: String, value: String) {
        if (redisCommands == null) return
        withContext(Dispatchers.IO) {
            redisCommands!!.set(key, value).whenCompleteAsync { _, _ ->
                if (LogUtils.enable()) {
                    LogUtils.d(RedisManager::class.java, "Redis set Complete! $key => $value")
                }
            }
        }
    }

    suspend fun remove(key: String) {
        if (redisCommands == null) return
        withContext(Dispatchers.IO) {
            redisCommands!!.del(key).whenCompleteAsync { _, _ ->
                if (LogUtils.enable()) {
                    LogUtils.d(RedisManager::class.java, "Redis remove key['$key'] Complete!")
                }
            }
        }
    }

}