package com.suborganizer.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.suborganizer.android.ui.theme.Amber
import com.suborganizer.android.ui.theme.AmberSoft
import com.suborganizer.android.ui.theme.BorderDark
import com.suborganizer.android.ui.theme.FuchsiaAccent
import com.suborganizer.android.ui.theme.IndigoAccent
import com.suborganizer.android.ui.theme.SurfaceRaised
import com.suborganizer.android.util.FREE_PLAN_LIMIT
import com.suborganizer.android.util.PRICING_URL
import com.suborganizer.android.util.openUrl

val BrandGradient = Brush.linearGradient(listOf(IndigoAccent, FuchsiaAccent))

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) BrandGradient else Brush.linearGradient(listOf(Color(0xFF3A3D4D), Color(0xFF3A3D4D))))
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(
                text = text.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceRaised)
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            .padding(padding),
    ) {
        content()
    }
}

@Composable
fun CategoryBadge(category: String) {
    val theme = com.suborganizer.android.ui.theme.categoryTheme(category)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(theme.soft)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = "${theme.emoji} $category",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

/** "Free plan · X of 3 tracked" pill — mirrors the badge on the web dashboard header. */
@Composable
fun FreePlanBadge(trackedCount: Int, modifier: Modifier = Modifier, limit: Int = FREE_PLAN_LIMIT) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AmberSoft.copy(alpha = 0.15f))
            .clickable { openUrl(context, PRICING_URL) }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = "Free plan · $trackedCount of $limit tracked",
            style = MaterialTheme.typography.labelSmall,
            color = Amber,
        )
    }
}

/** Every upgrade path in the app funnels here — checkout only ever happens on the website. */
@Composable
fun UpgradeToProButton(modifier: Modifier = Modifier, text: String = "Upgrade to Pro") {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BrandGradient)
            .clickable { openUrl(context, PRICING_URL) }
            .padding(vertical = 14.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(
                text = text.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
