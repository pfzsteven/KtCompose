package com.ktcompose.framework.api

import io.ktor.http.*

data class ApiCodes(
    val code: Int, var zh: String? = null, var tw: String? = null, var en: String? = null
) {

    fun getMessage(lang: String): String {
        if (code == HttpStatusCode.OK.value) {
            return "Success"
        }
        return when (lang) {
            "zh" -> zh
            "tw" -> tw
            else -> en
        } ?: "$code Error"
    }
}