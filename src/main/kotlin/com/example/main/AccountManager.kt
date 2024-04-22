package com.example.main

import com.ktcompose.framework.jwt.IJwtVerify

object AccountManager : IJwtVerify {
    override suspend fun buildPayload(map: HashMap<String, String>) {
        // map["package"] = "com.example.yourapp"
        // ...
    }

    override suspend fun verify(authentication: String, payload: String): Boolean {
        // if(authentication exists in your db) {
        //    return true
        // }
        // return false
        return true
    }
}