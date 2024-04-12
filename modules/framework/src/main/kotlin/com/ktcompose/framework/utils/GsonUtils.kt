package com.ktcompose.framework.utils

import com.google.gson.Gson

object GsonUtils {

    val gson: Gson by lazy { Gson() }

    inline fun <reified T> fromJson(json: String): T {
        return gson.fromJson(json, T::class.java)
    }

}