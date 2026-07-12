package com.suborganizer.android.data.repository

import com.suborganizer.android.data.SupabaseClientProvider
import com.suborganizer.android.data.model.Profile
import com.suborganizer.android.data.model.Subscription
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class SubscriptionRepository {
    private val client = SupabaseClientProvider.client

    suspend fun getSubscriptions(userId: String): List<Subscription> =
        client.from("subscriptions")
            .select {
                filter { eq("user_id", userId) }
                order("next_renewal_date", Order.ASCENDING)
            }
            .decodeList<Subscription>()

    suspend fun addSubscription(subscription: Subscription): Subscription =
        client.from("subscriptions")
            .insert(subscription) { select() }
            .decodeSingle<Subscription>()

    // Uses the builder-DSL form of update() (set(column, value) inside the update block)
    // rather than passing a raw value, per the confirmed Supabase Kotlin API —
    // see https://supabase.com/docs/reference/kotlin/update
    suspend fun updateSubscription(id: String, patch: Map<String, String?>) {
        client.from("subscriptions").update(
            {
                patch.forEach { (column, value) -> set(column, value) }
            },
        ) {
            filter { eq("id", id) }
        }
    }

    suspend fun deleteSubscription(id: String) {
        client.from("subscriptions").delete {
            filter { eq("id", id) }
        }
    }

    suspend fun getProfile(userId: String): Profile? =
        client.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<Profile>()

    suspend fun ensureProfile(userId: String, email: String?): Profile {
        return getProfile(userId) ?: client.from("profiles")
            .insert(Profile(id = userId, email = email, plan = "free")) { select() }
            .decodeSingle<Profile>()
    }
}
