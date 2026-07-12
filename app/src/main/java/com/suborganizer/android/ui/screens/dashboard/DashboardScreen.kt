package com.suborganizer.android.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suborganizer.android.data.model.Subscription
import com.suborganizer.android.ui.MainViewModel
import com.suborganizer.android.ui.components.GlassCard
import com.suborganizer.android.ui.components.MerchantAvatar
import com.suborganizer.android.ui.theme.AmberSoft
import com.suborganizer.android.ui.theme.IndigoAccent
import com.suborganizer.android.ui.theme.Muted
import com.suborganizer.android.util.Format

@Composable
fun DashboardScreen(
    mainViewModel: MainViewModel,
    onOpenCalendar: () -> Unit = {},
    onOpenAnalytics: () -> Unit = {},
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val currency = state.profile?.settings?.currency ?: "USD"

    val active = state.subscriptions.filter { it.status == "active" || it.status == "trial" }
    val totalMonthly = active.sumOf { Format.monthly(it.amount, it.billingCycle) }
    val trials = active.filter { it.status == "trial" && it.trialEndDate != null }
        .sortedBy { it.trialEndDate }
    val upcoming = active.filter {
        val d = Format.daysUntil(it.nextRenewalDate)
        d != null && d in 0..30
    }.sortedBy { it.nextRenewalDate }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(20.dp),
        )

        if (state.loading) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("TOTAL MONTHLY COMMITMENTS", style = MaterialTheme.typography.labelMedium, color = Muted)
                        Spacer(Modifier.height(4.dp))
                        Text(Format.currency(totalMonthly, currency), style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${Format.currency(totalMonthly * 12, currency)}/yr · ${active.size} active",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickLinkCard("Calendar", Icons.Default.CalendarMonth, Modifier.weight(1f), onOpenCalendar)
                    QuickLinkCard("Analytics", Icons.Default.BarChart, Modifier.weight(1f), onOpenAnalytics)
                }
            }

            if (trials.isNotEmpty()) {
                item { SectionLabel("⚠️ Before you're charged") }
                items(trials, key = { it.id ?: it.merchantName }) { sub ->
                    TrialRow(sub, currency)
                }
            }

            item { SectionLabel("Bills coming due (30d)") }
            if (upcoming.isEmpty()) {
                item { Text("Nothing due in the next 30 days.", color = Muted, modifier = Modifier.padding(vertical = 8.dp)) }
            } else {
                items(upcoming, key = { it.id ?: it.merchantName }) { sub ->
                    UpcomingRow(sub, currency)
                }
            }

            if (state.subscriptions.isEmpty()) {
                item {
                    Text(
                        "No subscriptions yet. Add one manually, or approve a detected candidate from Review.",
                        color = Muted,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = Muted)
}

@Composable
private fun QuickLinkCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier = modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(IndigoAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(18.dp))
            }
            Text(label, color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 10.dp))
        }
    }
}

@Composable
private fun TrialRow(sub: Subscription, currency: String) {
    val days = Format.daysUntil(sub.trialEndDate) ?: 0
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MerchantAvatar(sub.merchantName, sub.merchantDomain, sub.category, size = 32.dp)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(sub.merchantName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text("Bills ${Format.currency(sub.amount, currency)} in ${days}d", color = AmberSoft, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun UpcomingRow(sub: Subscription, currency: String) {
    val days = Format.daysUntil(sub.nextRenewalDate) ?: 0
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MerchantAvatar(sub.merchantName, sub.merchantDomain, sub.category, size = 32.dp)
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(sub.merchantName, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text("due in ${days}d", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        Text(Format.currency(sub.amount, currency), color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}
