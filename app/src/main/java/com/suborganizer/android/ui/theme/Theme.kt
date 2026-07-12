package com.suborganizer.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SubOrganizerColorScheme = darkColorScheme(
    primary = IndigoAccent,
    secondary = FuchsiaAccent,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = Foreground,
    onSurface = Foreground,
    error = Rose,
)

@Composable
fun SubOrganizerTheme(
    // The app is dark-only by design (matches the web app); the param exists
    // so a light theme can be added later without touching call sites.
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SubOrganizerColorScheme,
        typography = SubOrganizerTypography,
        content = content,
    )
}
