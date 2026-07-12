package com.suborganizer.android.detection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.suborganizer.android.data.model.Subscription
import com.suborganizer.android.data.repository.AuthRepository
import com.suborganizer.android.data.repository.DraftRepository
import com.suborganizer.android.data.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val ACTION_APPROVE_DRAFT = "com.suborganizer.android.action.APPROVE_DRAFT"
const val ACTION_DISCARD_DRAFT = "com.suborganizer.android.action.DISCARD_DRAFT"
const val EXTRA_DRAFT_ID = "draft_id"
const val EXTRA_NOTIFICATION_ID = "notification_id"

/**
 * Handles the Approve/Discard actions on a "detected a charge" notification without
 * needing to open the app — mirrors the Review screen's approve/discard flow but runs
 * entirely from the notification shade via goAsync(), same pattern as SmsReceiver.
 */
class DraftActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val draftId = intent.getStringExtra(EXTRA_DRAFT_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val appContext = context.applicationContext

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val draftRepository = DraftRepository(appContext)
                val draft = draftRepository.getById(draftId)
                if (draft != null) {
                    when (intent.action) {
                        ACTION_APPROVE_DRAFT -> {
                            val authRepository = AuthRepository()
                            val userId = authRepository.currentUserId
                            if (userId != null) {
                                SubscriptionRepository().addSubscription(
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
                            }
                        }
                        ACTION_DISCARD_DRAFT -> draftRepository.remove(draft.id)
                    }
                }
            } finally {
                if (notificationId != -1) {
                    NotificationManagerCompat.from(appContext).cancel(notificationId)
                }
                pendingResult.finish()
            }
        }
    }
}
