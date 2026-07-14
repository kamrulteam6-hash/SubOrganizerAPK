package com.suborganizer.android.ui.screens.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suborganizer.android.ui.components.GlassCard
import com.suborganizer.android.ui.components.UpgradeToProButton
import com.suborganizer.android.ui.theme.Emerald
import com.suborganizer.android.ui.theme.IndigoAccent
import com.suborganizer.android.ui.theme.Muted

// Mirrors the tiers in the web app's PricingSection.tsx — keep the two in sync.
private data class Tier(
    val name: String,
    val price: String,
    val period: String,
    val blurb: String,
    val highlight: Boolean,
    val features: List<String>,
)

private val TIERS = listOf(
    Tier(
        name = "Free",
        price = "$0",
        period = "forever",
        blurb = "Everything you need to take control.",
        highlight = false,
        features = listOf("Track up to 3 subscriptions", "Monthly & yearly totals", "Renewal reminders", "Browser extension (basic)", "Manual add"),
    ),
    Tier(
        name = "Pro",
        price = "$6",
        period = "/month",
        blurb = "For people serious about cutting waste.",
        highlight = true,
        features = listOf("Unlimited subscriptions", "All alert types", "Free-trial & price-hike alerts", "Full extension features", "Spending insights", "Priority support"),
    ),
    Tier(
        name = "Family",
        price = "$10",
        period = "/month",
        blurb = "Stop paying twice across your household.",
        highlight = false,
        features = listOf("Everything in Pro", "Up to 6 members", "Combined household total", "Duplicate subscription alerts", "Shared categories"),
    ),
)

@Composable
fun PricingScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Plans", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(
                "Start free. Upgrade only when your tracking needs expand — billing and account changes happen on the website.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(TIERS) { tier -> TierCard(tier) }
            item {
                Text(
                    "All plans include a risk-free 7-day Pro trial. Cancel anytime in one tap.",
                    color = Muted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun TierCard(tier: Tier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (tier.highlight) {
                    Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x1A6366F1))
                        .border(2.dp, IndigoAccent.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                } else {
                    Modifier
                },
            ),
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(20.dp)) {
            Column {
                if (tier.highlight) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0x336366F1)),
                    ) {
                        Text(
                            "MOST POPULAR",
                            color = IndigoAccent,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Text(tier.name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(tier.blurb, color = Muted, style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(tier.price, color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text(" ${tier.period}", color = Muted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                }

                Spacer(Modifier.height(16.dp))
                UpgradeToProButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (tier.name == "Free") "Get Started Free" else "Choose ${tier.name}",
                )

                Spacer(Modifier.height(16.dp))
                tier.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Emerald, modifier = Modifier.padding(top = 2.dp))
                        Text(feature, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}
