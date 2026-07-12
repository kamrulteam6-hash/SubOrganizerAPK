package com.suborganizer.android.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.suborganizer.android.R
import com.suborganizer.android.data.repository.AuthRepository
import com.suborganizer.android.data.repository.SubscriptionRepository
import com.suborganizer.android.util.Format

private const val CHANNEL_ID = "renewal_reminders"

/**
 * Runs periodically (see BootReceiver / WorkManager scheduling in MainActivity's
 * first launch) and posts a local notification for any subscription or trial
 * renewing within the user's configured lead time — same logic as the browser
 * extension's background.js alarm.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val authRepository = AuthRepository()
        val userId = authRepository.currentUserId ?: return Result.success()
        val subscriptionRepository = SubscriptionRepository()

        return try {
            val profile = subscriptionRepository.getProfile(userId)
            val leadDays = profile?.settings?.reminderLeadDays ?: 3
            val currency = profile?.settings?.currency ?: "USD"
            val subs = subscriptionRepository.getSubscriptions(userId)

            ensureChannel()

            subs.forEach { sub ->
                val dateToCheck = if (sub.status == "trial") sub.trialEndDate else sub.nextRenewalDate
                val days = Format.daysUntil(dateToCheck)
                if (days != null && days in 0..leadDays) {
                    notify(
                        id = sub.id.hashCode(),
                        title = if (sub.status == "trial") "Trial ending soon" else "Upcoming charge",
                        text = "${sub.merchantName} ${if (sub.status == "trial") "converts" else "renews"} for ${Format.currency(sub.amount, currency)} in ${days}d",
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Renewal reminders", NotificationManager.IMPORTANCE_DEFAULT)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun notify(id: Int, title: String, text: String) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        androidx.core.app.NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }
}
