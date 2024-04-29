package com.ktcompose.sign.facebook

import com.ktcompose.framework.http.HttpUtils
import com.ktcompose.framework.utils.LogUtils
import org.json.JSONObject

object FacebookSignManger {

    private const val VERIFY_URI = "https://graph.facebook.com/debug_token"

    suspend fun verifyToken(token: String): Boolean {
        val httpResult = HttpUtils.get(VERIFY_URI, HashMap<String, String>().apply {
            put("input_token", token)
            put("access_token", token)
        })
        return if (httpResult.success()) {
            val success: Boolean = httpResult.text?.let { json ->
                LogUtils.d(FacebookSignManger::class.java, json)
                val response = JSONObject(json)
                response.optJSONObject("data")?.let { data ->
                    data.optString("user_id") != null && data.optBoolean("is_valid")
                }
            } ?: false
            success
        } else {
            LogUtils.e(FacebookSignManger::class.java, "verifyToken fail.Http StatusCode:${httpResult.statusCode}")
            false
        }
    }
}