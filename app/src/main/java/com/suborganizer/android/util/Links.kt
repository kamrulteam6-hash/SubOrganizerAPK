package com.suborganizer.android.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Free-plan tracked-subscription cap — mirrors LIMIT in the web app's DashboardHome.tsx. */
const val FREE_PLAN_LIMIT = 3

/** Anchors the pricing section on the marketing site — checkout itself only ever happens there. */
const val PRICING_URL = "https://suborganizer.app/#pricing"

fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
