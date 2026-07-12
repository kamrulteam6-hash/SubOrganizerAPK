package com.suborganizer.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.suborganizer.android.MainActivity
import com.suborganizer.android.R
import com.suborganizer.android.data.model.Subscription
import com.suborganizer.android.data.repository.AuthRepository
import com.suborganizer.android.data.repository.SubscriptionRepository
import com.suborganizer.android.util.Format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Plain AppWidgetProvider + RemoteViews rather than Jetpack Glance — avoids pulling in
// another Compose-adjacent dependency whose version compatibility with this project's
// already-bleeding-edge AGP/Compose BOM can't be verified without a real build.
class RenewalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = AuthRepository().currentUserId
                val upcoming = if (userId != null) {
                    runCatching { SubscriptionRepository().getSubscriptions(userId) }
                        .getOrDefault(emptyList())
                        .filter { (it.status == "active" || it.status == "trial") && it.nextRenewalDate != null }
                        .sortedBy { it.nextRenewalDate }
                        .take(3)
                } else {
                    emptyList()
                }
                appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id, upcoming) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, subs: List<Subscription>) {
        val views = RemoteViews(context.packageName, R.layout.widget_renewals)

        val rows = listOf(
            Triple(R.id.widget_row_1, R.id.widget_merchant_1, R.id.widget_amount_1),
            Triple(R.id.widget_row_2, R.id.widget_merchant_2, R.id.widget_amount_2),
            Triple(R.id.widget_row_3, R.id.widget_merchant_3, R.id.widget_amount_3),
        )

        rows.forEachIndexed { index, (rowId, nameId, amountId) ->
            val sub = subs.getOrNull(index)
            if (sub == null) {
                views.setViewVisibility(rowId, View.GONE)
            } else {
                views.setViewVisibility(rowId, View.VISIBLE)
                views.setTextViewText(nameId, sub.merchantName)
                views.setTextViewText(amountId, Format.currency(sub.amount))
            }
        }
        views.setViewVisibility(R.id.widget_empty, if (subs.isEmpty()) View.VISIBLE else View.GONE)

        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
