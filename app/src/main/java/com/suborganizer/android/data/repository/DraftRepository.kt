package com.suborganizer.android.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.suborganizer.android.data.model.DraftSubscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.draftsDataStore by preferencesDataStore(name = "drafts")
private val DRAFTS_KEY = stringPreferencesKey("draft_subscriptions_json")

/**
 * On-device, unsynced queue of subscription candidates collected from notifications/SMS,
 * pending user review — mirrors the browser extension's chrome.storage.local drafts.
 * Nothing here reaches Supabase until [SubscriptionRepository.addSubscription] is called
 * for an approved draft.
 */
class DraftRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    val drafts: Flow<List<DraftSubscription>> = context.draftsDataStore.data.map { prefs ->
        val raw = prefs[DRAFTS_KEY] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<DraftSubscription>>(raw) }.getOrDefault(emptyList())
    }

    /** Returns the saved draft, or null if it was a duplicate and nothing was added. */
    suspend fun addIfNew(draft: DraftSubscription): DraftSubscription? {
        val current = drafts.first()
        val exists = current.any {
            (it.merchantDomain != null && it.merchantDomain == draft.merchantDomain) ||
                it.merchantName.equals(draft.merchantName, ignoreCase = true)
        }
        if (exists) return null
        save(current + draft)
        return draft
    }

    suspend fun getById(id: String): DraftSubscription? = drafts.first().find { it.id == id }

    suspend fun remove(id: String) {
        save(drafts.first().filterNot { it.id == id })
    }

    suspend fun clear() {
        save(emptyList())
    }

    private suspend fun save(list: List<DraftSubscription>) {
        context.draftsDataStore.edit { prefs ->
            prefs[DRAFTS_KEY] = json.encodeToString(list)
        }
    }
}
