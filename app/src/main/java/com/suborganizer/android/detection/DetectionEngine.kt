package com.suborganizer.android.detection

import com.suborganizer.android.data.model.DraftSource
import com.suborganizer.android.data.model.DraftSubscription
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Ports the browser extension's confidence-scoring approach (extension-src/content.js)
 * to short-form text: SMS bodies and notification title+text, instead of full page text.
 * Nothing here ever leaves the device — a match just becomes a local [DraftSubscription]
 * for the user to review, same as the extension's draft queue.
 */
object DetectionEngine {

    private val MERCHANT_KEYWORDS = mapOf(
        "netflix" to ("Netflix" to "streaming"),
        "spotify" to ("Spotify" to "music"),
        "youtube premium" to ("YouTube Premium" to "streaming"),
        "youtube music" to ("YouTube Music" to "music"),
        "disney+" to ("Disney+" to "streaming"),
        "disney plus" to ("Disney+" to "streaming"),
        "hbo max" to ("HBO Max" to "streaming"),
        "hulu" to ("Hulu" to "streaming"),
        "amazon prime" to ("Amazon Prime" to "other"),
        "apple music" to ("Apple Music" to "music"),
        "apple tv" to ("Apple TV+" to "streaming"),
        "icloud" to ("iCloud+" to "cloud"),
        "google one" to ("Google One" to "cloud"),
        "chatgpt" to ("ChatGPT Plus" to "software"),
        "openai" to ("ChatGPT Plus" to "software"),
        "claude" to ("Claude Pro" to "software"),
        "anthropic" to ("Claude Pro" to "software"),
        "notion" to ("Notion" to "software"),
        "figma" to ("Figma" to "software"),
        "canva" to ("Canva Pro" to "software"),
        "adobe" to ("Adobe" to "software"),
        "dropbox" to ("Dropbox" to "cloud"),
        "github" to ("GitHub" to "software"),
        "slack" to ("Slack" to "software"),
        "zoom" to ("Zoom" to "software"),
        "grammarly" to ("Grammarly" to "software"),
        "audible" to ("Audible" to "other"),
        "duolingo" to ("Duolingo Plus" to "other"),
        "headspace" to ("Headspace" to "fitness"),
        "calm" to ("Calm" to "fitness"),
        "strava" to ("Strava" to "fitness"),
        "nordvpn" to ("NordVPN" to "software"),
        "expressvpn" to ("ExpressVPN" to "software"),
        "1password" to ("1Password" to "software"),
        "microsoft 365" to ("Microsoft 365" to "software"),
        "linkedin premium" to ("LinkedIn Premium" to "other"),
    )

    private val AMOUNT_REGEX = Regex(
        "(?:USD|US\\$|CAD|AUD|INR|BDT|EUR|GBP|Rs\\.?|₹|৳|\\$|€|£|¥)\\s?([\\d,]+(?:\\.\\d{1,2})?)",
        RegexOption.IGNORE_CASE,
    )

    private val RECURRING_KEYWORDS = Regex(
        "subscription|renew|auto-?pay|auto-?debit|recurring|membership|billed|debited|charged|payment (successful|received)|trial",
        RegexOption.IGNORE_CASE,
    )

    private val TRIAL_KEYWORDS = Regex("free trial|trial period|trial end|trial expir", RegexOption.IGNORE_CASE)

    private val NOISE_KEYWORDS = Regex(
        "otp|one.?time password|verification code|delivery|shipped|arriving|balance enquiry|login attempt",
        RegexOption.IGNORE_CASE,
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun analyze(
        rawText: String,
        source: DraftSource,
        sourceApp: String? = null,
    ): DraftSubscription? {
        if (rawText.isBlank() || NOISE_KEYWORDS.containsMatchIn(rawText)) return null

        val lower = rawText.lowercase()
        val merchantEntry = MERCHANT_KEYWORDS.entries.firstOrNull { lower.contains(it.key) }
        val hasRecurringSignal = RECURRING_KEYWORDS.containsMatchIn(rawText)
        val amount = extractAmount(rawText)

        // Require either a known merchant OR a strong recurring-payment signal + amount —
        // avoids flagging one-off purchases or unrelated bank messages as subscriptions.
        if (merchantEntry == null && !(hasRecurringSignal && amount != null)) return null

        var confidence = 0.0
        if (merchantEntry != null) confidence += 0.5
        if (hasRecurringSignal) confidence += 0.3
        if (amount != null) confidence += 0.2
        if (confidence < 0.5) return null

        val isTrial = TRIAL_KEYWORDS.containsMatchIn(rawText)
        val merchantName = merchantEntry?.value?.first ?: guessMerchantName(rawText, sourceApp)
        val category = merchantEntry?.value?.second ?: "other"
        val nextDate = extractDate(rawText) ?: defaultRenewalDate(isTrial)

        return DraftSubscription(
            merchantName = merchantName,
            merchantDomain = null,
            category = category,
            amount = amount,
            billingCycle = extractCycle(rawText) ?: "monthly",
            status = if (isTrial) "trial" else "active",
            trialEndDate = if (isTrial) nextDate else null,
            nextRenewalDate = nextDate,
            source = source,
            sourceApp = sourceApp,
            rawText = rawText.take(500),
            confidence = minOf(confidence, 0.95),
        )
    }

    private fun extractAmount(text: String): Double? {
        val matches = AMOUNT_REGEX.findAll(text)
            .mapNotNull { it.groupValues[1].replace(",", "").toDoubleOrNull() }
            .filter { it > 0 && it < 100000 }
            .toList()
        return matches.firstOrNull()
    }

    private fun extractCycle(text: String): String? {
        val t = text.lowercase()
        return when {
            Regex("per\\s?year|/\\s?year|annually|yearly").containsMatchIn(t) -> "yearly"
            Regex("per\\s?quarter|quarterly").containsMatchIn(t) -> "quarterly"
            Regex("per\\s?week|weekly").containsMatchIn(t) -> "weekly"
            Regex("per\\s?month|/\\s?month|monthly|/mo\\b").containsMatchIn(t) -> "monthly"
            else -> null
        }
    }

    private fun extractDate(text: String): String? {
        // Basic "on <Month> <day>" / "yyyy-MM-dd" pattern support; falls back to a
        // default offset when the message doesn't spell out a date explicitly.
        val isoMatch = Regex("(\\d{4}-\\d{2}-\\d{2})").find(text)
        if (isoMatch != null) return isoMatch.groupValues[1]
        return null
    }

    private fun defaultRenewalDate(isTrial: Boolean): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, if (isTrial) 7 else 30)
        return dateFormat.format(cal.time)
    }

    private fun guessMerchantName(text: String, sourceApp: String?): String {
        sourceApp?.let { return it }
        val firstWords = text.trim().split(Regex("\\s+")).take(3).joinToString(" ")
        return firstWords.ifBlank { "Unknown merchant" }
    }
}
