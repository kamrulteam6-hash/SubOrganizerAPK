package com.suborganizer.android.detection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.suborganizer.android.data.model.DraftSource
import com.suborganizer.android.data.repository.DraftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reads incoming SMS bodies on-device to catch bank/payment-provider "you were charged"
 * texts the same way the browser extension reads checkout pages. See README for the
 * Play Store distribution note on RECEIVE_SMS/READ_SMS — this receiver is inert unless
 * the user has explicitly granted SMS permission.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        val sender = messages.firstOrNull()?.originatingAddress
        if (body.isBlank()) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val draft = DetectionEngine.analyze(body, DraftSource.SMS, sender)
                if (draft != null) {
                    DraftRepository(appContext).addIfNew(draft)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
