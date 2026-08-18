package com.example.remindmehere

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.remindmehere.theme.DeepNavy
import com.example.remindmehere.ui.components.BottomNavBar
import com.example.remindmehere.ui.components.BottomNavDest
import com.example.remindmehere.ui.screens.DashboardScreen
import com.example.remindmehere.ui.screens.NearbyScreen
import com.example.remindmehere.ui.screens.UpcomingScreen

import com.example.remindmehere.ui.screens.HistoryScreen

@Composable
fun MainNavigation() {
    var currentDest by remember { mutableStateOf<BottomNavDest>(BottomNavDest.Dashboard) }
    var showHistory by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = showHistory,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith fadeOut()
            } else {
                fadeIn() togetherWith slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            }
        },
        label = "main_nav"
    ) { history ->
        if (history) {
            HistoryScreen(onBack = { showHistory = false })
        } else {
            Scaffold(
                containerColor = DeepNavy,
                bottomBar = {
                    BottomNavBar(
                        current = currentDest,
                        onNavigate = { currentDest = it }
                    )
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentDest,
                    modifier = Modifier.padding(innerPadding),
                    transitionSpec = {
                        fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
                    },
                    label = "nav"
                ) { dest ->
                    when (dest) {
                        BottomNavDest.Dashboard -> DashboardScreen(onNavigateToHistory = { showHistory = true })
                        BottomNavDest.Upcoming  -> UpcomingScreen(onNavigateToHistory = { showHistory = true })
                        BottomNavDest.Nearby    -> NearbyScreen(onNavigateToHistory = { showHistory = true })
                    }
                }
            }
        }
    }
}
