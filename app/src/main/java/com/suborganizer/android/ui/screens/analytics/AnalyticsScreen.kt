package com.suborganizer.android.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
import com.suborganizer.android.ui.MainViewModel
import com.suborganizer.android.ui.components.GlassCard
import com.suborganizer.android.ui.theme.AmberSoft
import com.suborganizer.android.ui.theme.Emerald
import com.suborganizer.android.ui.theme.Muted
import com.suborganizer.android.ui.theme.RoseSoft
import com.suborganizer.android.ui.theme.categoryTheme
import com.suborganizer.android.util.Format

private data class Insight(val type: String, val title: String, val text: String)

@Composable
fun AnalyticsScreen(mainViewModel: MainViewModel) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val currency = state.profile?.settings?.currency ?: "USD"
    val active = state.subscriptions.filter { it.status == "active" || it.status == "trial" }
    val totalMonthly = active.sumOf { Format.monthly(it.amount, it.billingCycle) }

    val categorySums = active
        .groupBy { it.category.ifBlank { "other" } }
        .mapValues { (_, subs) -> subs.sumOf { Format.monthly(it.amount, it.billingCycle) } }
        .toList()
        .sortedByDescending { it.second }

    val insights = buildInsights(active.size, totalMonthly, currency, active.count { it.category == "software" }, active.count { it.billingCycle == "yearly" }, active.count { it.status == "trial" })

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Analytics & Insights",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(20.dp),
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("MONTHLY SPEND", style = MaterialTheme.typography.labelMedium, color = Muted)
                        Spacer(Modifier.height(4.dp))
                        Text(Format.currency(totalMonthly, currency), style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text("${Format.currency(totalMonthly * 12, currency)} / year across ${active.size} active", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("SPEND BY CATEGORY", style = MaterialTheme.typography.labelMedium, color = Muted)
                        Spacer(Modifier.height(12.dp))
                        if (categorySums.isEmpty()) {
                            Text("No active subscriptions yet.", color = Muted)
                        } else {
                            categorySums.forEach { (category, amount) ->
                                CategoryBar(category, amount, totalMonthly, currency)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            item {
                Text("SENTINEL INSIGHTS", style = MaterialTheme.typography.labelMedium, color = Muted, modifier = Modifier.padding(top = 4.dp))
            }
            items(insights) { insight -> InsightCard(insight) }
        }
    }
}

@Composable
private fun CategoryBar(category: String, amount: Double, total: Double, currency: String) {
    val theme = categoryTheme(category)
    val fraction = if (total > 0) (amount / total).toFloat().coerceIn(0f, 1f) else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${theme.emoji} ${category.replaceFirstChar { it.uppercase() }}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text("${Format.currency(amount, currency)} (${(fraction * 100).toInt()}%)", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(6.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.06f)),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(theme.gradientStart),
            )
        }
    }
}

@Composable
private fun InsightCard(insight: Insight) {
    val (icon, tint) = when (insight.type) {
        "warn" -> Icons.Default.Warning to RoseSoft
        "success" -> Icons.Default.TrendingUp to Emerald
        else -> Icons.Default.Info to AmberSoft
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(top = 2.dp))
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(insight.title, color = tint, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(insight.text, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun buildInsights(
    activeCount: Int,
    totalMonthly: Double,
    currency: String,
    softwareCount: Int,
    yearlyCount: Int,
    trialCount: Int,
): List<Insight> {
    val list = mutableListOf<Insight>()
    if (totalMonthly > 120) {
        list.add(
            Insight(
                "warn",
                "High Monthly Commitment",
                "Your active subscriptions total ${Format.currency(totalMonthly, currency)}/month — worth a pass to see what's actually used.",
            ),
        )
    }
    if (softwareCount >= 3) {
        list.add(Insight("tip", "Software Overlap", "You have $softwareCount software subscriptions active — check for redundant tools."))
    }
    if (activeCount > 4 && yearlyCount == 0) {
        list.add(Insight("tip", "Annual Billing Savings", "None of your subscriptions bill yearly — switching durable tools to annual often saves 15-20%."))
    }
    if (trialCount > 0) {
        list.add(Insight("warn", "Active Free Trials", "You have $trialCount trial(s) running — check the Dashboard so none convert unnoticed."))
    }
    if (list.isEmpty()) {
        list.add(Insight("success", "Clean Budget", "No red flags — your active subscriptions look well managed."))
    }
    return list
}
