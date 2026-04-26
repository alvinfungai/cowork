package com.alvinfungai.app.core

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val usdFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    fun formatUsd(amount: Double): String {
        return usdFormatter.format(amount)
    }
}
