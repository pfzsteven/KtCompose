package com.ktcompose.framework.utils

import kotlinx.datetime.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TimeUtils {
    fun nowEpochMilli() = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli()

    fun nowEpochMilliInstant() =
        Instant.fromEpochMilliseconds(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli())

    fun dateStringOfPattern(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val dateTime = LocalDateTime.now(ZoneOffset.of("+8"))
        return dateTime.format(formatter)
    }

    fun dateStringOfPattern(pattern: String = "yyyy-MM-dd HH:mm:ss", plusMinutes: Long): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val dateTime = LocalDateTime.now(ZoneOffset.of("+8")).plusMinutes(plusMinutes)
        return dateTime.format(formatter)
    }

    fun timestampFromDateString(pattern: String = "yyyy-MM-dd HH:mm:ss", dateTime: String): Long {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.parse(dateTime, formatter).toInstant(ZoneOffset.of("+8")).toEpochMilli()
    }

    fun timestampToDateString(pattern: String = "yyyy-MM-dd HH:mm:ss", timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.of("+8"))
        return dateTime.format(formatter)
    }
}