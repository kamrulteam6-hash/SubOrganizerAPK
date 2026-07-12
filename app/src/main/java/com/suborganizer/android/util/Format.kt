package com.suborganizer.android.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

object Format {
    fun currency(amount: Double, code: String = "USD"): String = try {
        val nf = NumberFormat.getCurrencyInstance(Locale.US)
        nf.currency = Currency.getInstance(code)
        nf.format(amount)
    } catch (e: Exception) {
        "$${"%.2f".format(amount)}"
    }

    fun monthly(amount: Double, cycle: String): Double = when (cycle) {
        "monthly" -> amount
        "yearly" -> amount / 12
        "weekly" -> amount * 52 / 12
        "quarterly" -> amount / 3
        else -> 0.0
    }

    /** Days from today until [dateStr] (yyyy-MM-dd), or null if unparseable. */
    fun daysUntil(dateStr: String?): Int? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val target = Calendar.getInstance().apply { time = fmt.parse(dateStr)!! }
            val now = Calendar.getInstance()
            listOf(target, now).forEach {
                it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0); it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
            }
            val diffMs = target.timeInMillis - now.timeInMillis
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            null
        }
    }

    fun todayPlus(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
}
