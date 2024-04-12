package com.ktcompose.framework.env

object ServerEnvManager {

    private var currentServerEnv: ServerEnv = ServerEnv.Release

    @JvmStatic
    fun getApiEnv() = currentServerEnv

    @JvmStatic
    fun init(env: String = "") {
        if (env.isNotEmpty()) {
            currentServerEnv = when (env.uppercase()) {
                "DEBUG" -> {
                    ServerEnv.Debug
                }

                "TEST" -> {
                    ServerEnv.Test
                }

                else -> {
                    ServerEnv.Release
                }
            }
        }
    }
}