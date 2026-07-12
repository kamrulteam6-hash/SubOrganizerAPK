package com.suborganizer.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors the `subscriptions` table used by the web app and browser extension —
// same Supabase project, same schema, so rows created here show up everywhere.
@Serializable
data class Subscription(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("merchant_name") val merchantName: String,
    @SerialName("merchant_domain") val merchantDomain: String? = null,
    val category: String = "other",
    val amount: Double,
    @SerialName("billing_cycle") val billingCycle: String = "monthly",
    val currency: String = "USD",
    val status: String = "active",
    @SerialName("trial_end_date") val trialEndDate: String? = null,
    @SerialName("next_renewal_date") val nextRenewalDate: String? = null,
    @SerialName("detection_source") val detectionSource: String = "manual",
    val confidence: Double = 1.0,
    @SerialName("cancel_url") val cancelUrl: String? = null,
    val notes: String? = null,
)

@Serializable
data class Profile(
    val id: String,
    val email: String? = null,
    val plan: String? = "free",
    val settings: ProfileSettings? = null,
)

@Serializable
data class ProfileSettings(
    @SerialName("reminder_lead_days") val reminderLeadDays: Int? = 3,
    val currency: String? = "USD",
)
