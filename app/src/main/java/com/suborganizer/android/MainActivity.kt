package com.suborganizer.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suborganizer.android.data.repository.AuthRepository
import com.suborganizer.android.ui.MainViewModel
import com.suborganizer.android.ui.nav.Destinations
import com.suborganizer.android.ui.screens.add.AddSubscriptionScreen
import com.suborganizer.android.ui.screens.analytics.AnalyticsScreen
import com.suborganizer.android.ui.screens.auth.LoginScreen
import com.suborganizer.android.ui.screens.calendar.CalendarScreen
import com.suborganizer.android.ui.screens.dashboard.DashboardScreen
import com.suborganizer.android.ui.screens.pricing.PricingScreen
import com.suborganizer.android.ui.screens.review.ReviewScreen
import com.suborganizer.android.ui.screens.settings.SettingsScreen
import com.suborganizer.android.ui.screens.subscriptions.SubscriptionsScreen
import com.suborganizer.android.ui.theme.BackgroundDark
import com.suborganizer.android.ui.theme.IndigoAccent
import com.suborganizer.android.ui.theme.Muted
import com.suborganizer.android.ui.theme.SubOrganizerTheme
import com.suborganizer.android.work.ReminderScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubOrganizerTheme {
                SubOrganizerRoot()
            }
        }
    }
}

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// Kept to 4 tabs deliberately — Add is a FAB, Calendar/Analytics are one tap away
// from Dashboard's quick links, so the bar doesn't get cramped on smaller phones.
private val bottomTabs = listOf(
    BottomTab(Destinations.DASHBOARD, "Home", Icons.Default.Home),
    BottomTab(Destinations.SUBSCRIPTIONS, "Subs", Icons.Default.CreditCard),
    BottomTab(Destinations.REVIEW, "Review", Icons.Default.NotificationsActive),
    BottomTab(Destinations.SETTINGS, "Settings", Icons.Default.Settings),
)

// Named ...Root, not ...App: a composable called SubOrganizerApp() would be ambiguous
// with the SubOrganizerApp Application class constructor in this same package.
@Composable
private fun SubOrganizerRoot() {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    var loggedIn by remember { mutableStateOf(authRepository.currentUserId != null) }
    val mainViewModel: MainViewModel = viewModel()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* no-op: reminders simply won't show if denied, nothing else depends on this */ }

    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            mainViewModel.refresh()
            ReminderScheduler.schedule(context.applicationContext)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (!loggedIn) {
        LoginScreen(onLoggedIn = { loggedIn = true })
        return
    }

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            // Only show the Add FAB on screens where "add a subscription" makes sense.
            if (currentRoute == Destinations.DASHBOARD || currentRoute == Destinations.SUBSCRIPTIONS) {
                FloatingActionButton(
                    onClick = { navController.navigate(Destinations.ADD) },
                    containerColor = IndigoAccent,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add subscription", tint = Color.White)
                }
            }
        },
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar(containerColor = Color(0xFF0A0B12)) {
                bottomTabs.forEach { tab ->
                    val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoAccent,
                            selectedTextColor = IndigoAccent,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted,
                            indicatorColor = Color(0x1F6366F1),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = Destinations.DASHBOARD) {
                composable(Destinations.DASHBOARD) {
                    DashboardScreen(
                        mainViewModel = mainViewModel,
                        onOpenCalendar = { navController.navigate(Destinations.CALENDAR) },
                        onOpenAnalytics = { navController.navigate(Destinations.ANALYTICS) },
                        onOpenPricing = { navController.navigate(Destinations.PRICING) },
                    )
                }
                composable(Destinations.SUBSCRIPTIONS) { SubscriptionsScreen(mainViewModel) }
                composable(Destinations.REVIEW) { ReviewScreen(mainViewModel) }
                composable(Destinations.CALENDAR) { CalendarScreen(mainViewModel) }
                composable(Destinations.ANALYTICS) { AnalyticsScreen(mainViewModel) }
                composable(Destinations.PRICING) { PricingScreen() }
                composable(Destinations.ADD) {
                    AddSubscriptionScreen(
                        mainViewModel = mainViewModel,
                        onSaved = { navController.popBackStack() },
                        onOpenPricing = { navController.navigate(Destinations.PRICING) },
                    )
                }
                composable(Destinations.SETTINGS) {
                    SettingsScreen(
                        mainViewModel = mainViewModel,
                        onSignedOut = { loggedIn = false },
                        onOpenPricing = { navController.navigate(Destinations.PRICING) },
                    )
                }
            }
        }
    }
}
