package com.suborganizer.android.data.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * A subscription candidate collected on-device (from a notification or SMS) that has
 * NOT been saved yet. Lives only in local storage (see DraftRepository) until the user
 * reviews and approves it — mirrors the browser extension's "drafts" queue.
 */
@Serializable
data class DraftSubscription(
    val id: String = Random.nextBytes(8).joinToString("") { "%02x".format(it) },
    val merchantName: String,
    val merchantDomain: String? = null,
    val category: String = "other",
    val amount: Double? = null,
    val billingCycle: String = "monthly",
    val status: String = "active",
    val trialEndDate: String? = null,
    val nextRenewalDate: String? = null,
    val source: DraftSource,
    val sourceApp: String? = null,
    val rawText: String,
    val confidence: Double,
    val detectedAt: Long = System.currentTimeMillis(),
)

@Serializable
enum class DraftSource { NOTIFICATION, SMS }
