package com.alvinfungai.app.core

import java.util.concurrent.TimeUnit

object TimeUtils {
    fun formatLastActive(lastActiveMillis: Long?): String {
        if (lastActiveMillis == null) return "Inactive"
        
        val diff = System.currentTimeMillis() - lastActiveMillis
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)

        return when {
            minutes < 1 -> "Now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> "A long time ago"
        }
    }
}
