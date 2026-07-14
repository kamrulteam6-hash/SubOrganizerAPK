package com.suborganizer.android.ui

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.suborganizer.android.data.model.DraftSubscription
import com.suborganizer.android.data.model.Profile
import com.suborganizer.android.data.model.Subscription
import com.suborganizer.android.data.repository.AuthRepository
import com.suborganizer.android.data.repository.DraftRepository
import com.suborganizer.android.data.repository.SubscriptionRepository
import com.suborganizer.android.detection.UpgradeNotifier
import com.suborganizer.android.util.FREE_PLAN_LIMIT
import com.suborganizer.android.widget.RenewalWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val loading: Boolean = true,
    val subscriptions: List<Subscription> = emptyList(),
    val profile: Profile? = null,
    val error: String? = null,
)

/**
 * Shared state for everything behind login — subscriptions, profile, and the local
 * draft review queue — so screens don't each re-fetch independently.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val subscriptionRepository = SubscriptionRepository()
    private val draftRepository = DraftRepository(application)

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state

    val drafts: StateFlow<List<DraftSubscription>> = draftRepository.drafts
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserEmail: String? get() = authRepository.currentUserEmail

    fun refresh() {
        val userId = authRepository.currentUserId ?: return
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch { refreshSuspend(userId) }
    }

    // Extracted so callers already inside a coroutine (like addSubscription) can await
    // the reload directly instead of firing a detached coroutine via refresh() and
    // reading _state.value before it's actually updated.
    private suspend fun refreshSuspend(userId: String) {
        try {
            val profile = subscriptionRepository.ensureProfile(userId, authRepository.currentUserEmail)
            val subs = subscriptionRepository.getSubscriptions(userId)
            _state.value = _state.value.copy(loading = false, subscriptions = subs, profile = profile)
            requestWidgetUpdate()
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message ?: "Failed to load your subscriptions.")
        }
    }

    // Pushes a live refresh to any placed home-screen widgets right after data changes,
    // instead of leaving them to catch up on the next system-scheduled update (which can
    // be hours away — updatePeriodMillis has an OS-enforced 30-minute floor).
    private fun requestWidgetUpdate() {
        val context = getApplication<Application>().applicationContext
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, RenewalWidgetProvider::class.java))
        if (ids.isNotEmpty()) {
            val intent = Intent(context, RenewalWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }

    fun addSubscription(subscription: Subscription, onDone: (Result<Unit>) -> Unit) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            try {
                subscriptionRepository.addSubscription(subscription.copy(userId = userId))
                refreshSuspend(userId)
                notifyIfJustHitFreeLimit()
                onDone(Result.success(Unit))
            } catch (e: Exception) {
                onDone(Result.failure(e))
            }
        }
    }

    // Fires once, right as a free-plan user's count crosses into the cap — refreshSuspend()
    // has already repopulated _state.value by the time this runs, so this only ever
    // sees the count settle exactly at the limit the one time it's newly reached.
    private fun notifyIfJustHitFreeLimit() {
        val current = _state.value
        val isFreePlan = current.profile?.plan.isNullOrBlank() || current.profile?.plan == "free"
        if (isFreePlan && current.subscriptions.size == FREE_PLAN_LIMIT) {
            UpgradeNotifier.notify(getApplication<Application>().applicationContext)
        }
    }

    fun updateSubscriptionStatus(id: String, status: String) {
        viewModelScope.launch {
            subscriptionRepository.updateSubscription(id, mapOf("status" to status))
            refresh()
        }
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch {
            subscriptionRepository.deleteSubscription(id)
            refresh()
        }
    }

    fun approveDraft(draft: DraftSubscription) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            subscriptionRepository.addSubscription(
                Subscription(
                    userId = userId,
                    merchantName = draft.merchantName,
                    merchantDomain = draft.merchantDomain,
                    category = draft.category,
                    amount = draft.amount ?: 0.0,
                    billingCycle = draft.billingCycle,
                    status = draft.status,
                    trialEndDate = draft.trialEndDate,
                    nextRenewalDate = draft.nextRenewalDate,
                    detectionSource = if (draft.source.name == "SMS") "sms" else "notification",
                    confidence = draft.confidence,
                ),
            )
            draftRepository.remove(draft.id)
            refresh()
        }
    }

    fun discardDraft(id: String) {
        viewModelScope.launch { draftRepository.remove(id) }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onDone()
        }
    }
}
