package com.ketcompose.main

import com.ktcompose.engine.ServerEngine
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*

fun main() {
    // 统一调用
    val environment = applicationEngineEnvironment {
        ServerEngine.start(this) { application ->
            // ... do something here ...
        }
    }
    embeddedServer(Tomcat, environment).start(wait = true)
}
