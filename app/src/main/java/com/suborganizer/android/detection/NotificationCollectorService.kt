package com.suborganizer.android.detection

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.suborganizer.android.data.model.DraftSource
import com.suborganizer.android.data.repository.DraftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Reads notification title/text from other apps (banking apps, Gmail/Outlook previews,
 * Google Pay, Play Billing receipts) entirely on-device. Only a parsed, structured
 * candidate — never the raw notification — is written to the local draft queue; nothing
 * is sent anywhere until the user approves it in the Review screen.
 *
 * Requires the user to grant "Notification access" manually in Android Settings —
 * this is the Play-Store-compliant collection channel (see AndroidManifest comment).
 */
class NotificationCollectorService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var draftRepository: DraftRepository

    // A quick pre-filter so we don't run full text analysis on every single notification
    // system-wide (chat messages, media controls, etc.) — cheap to compute, avoids
    // wasted CPU/battery before handing off to DetectionEngine's real scoring.
    private val quickPreFilter = Regex(
        "\\d.*(rs\\.?|₹|৳|\\$|€|£|usd|inr|bdt)|(?:rs\\.?|₹|৳|\\$|€|£)\\s?\\d|subscription|renew|trial|billed|debited|charged",
        RegexOption.IGNORE_CASE,
    )

    override fun onCreate() {
        super.onCreate()
        draftRepository = DraftRepository(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        val extras = notification.notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString().orEmpty()
        val combined = "$title $text".trim()
        if (combined.isBlank() || !quickPreFilter.containsMatchIn(combined)) return

        val appLabel = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(notification.packageName, 0)).toString()
        }.getOrNull()

        val draft = DetectionEngine.analyze(combined, DraftSource.NOTIFICATION, appLabel) ?: return

        scope.launch {
            draftRepository.addIfNew(draft)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
