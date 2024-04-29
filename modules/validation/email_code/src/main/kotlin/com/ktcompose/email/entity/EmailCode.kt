package com.ktcompose.email.entity

/**
 * c: code
 * e: expired time
 */
data class EmailCode(val c: String) {
    companion object {
        /**
         * 60 seconds
         */
        const val EXPIRED_DURATION = 60_000L
    }

    val e: Long = System.currentTimeMillis() + EXPIRED_DURATION
}
