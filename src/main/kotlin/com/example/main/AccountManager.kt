package com.example.main

import com.ktcompose.framework.jwt.IJwtVerify

object AccountManager : IJwtVerify {
    override suspend fun buildPayload(map: HashMap<String, String>) {
    }
}