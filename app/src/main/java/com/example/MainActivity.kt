package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FitTrackViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: FitTrackViewModel = viewModel()
                val userProfile by viewModel.userProfile.collectAsState()

                if (userProfile == null || !userProfile!!.isOnboarded) {
                    OnboardingScreen(
                        onComplete = { newProfile ->
                            viewModel.saveProfile(newProfile)
                        }
                    )
                } else {
                    FitTrackAppShell(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun FitTrackAppShell(viewModel: FitTrackViewModel) {
    var currentDestination by remember { mutableStateOf("Dashboard") }

    val navItems = listOf(
        NavItem("Dashboard", "Home", Icons.Default.Home),
        NavItem("Diet", "Diet", Icons.Default.RestaurantMenu),
        NavItem("Workouts", "Workouts", Icons.Default.FitnessCenter),
        NavItem("Photos", "Photos", Icons.Default.PhotoLibrary),
        NavItem("AI Coach", "AI Coach", Icons.Default.Chat),
        NavItem("Settings", "Settings", Icons.Default.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.height(80.dp)
            ) {
                navItems.forEach { item ->
                    val isSelected = currentDestination == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = item.route },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("nav_${item.route.lowercase().replace(" ", "_")}"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp) // Leave small padding for bottom nav
        ) {
            when (currentDestination) {
                "Dashboard" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToWorkouts = { currentDestination = "Workouts" },
                    modifier = Modifier.padding(innerPadding)
                )
                "Diet" -> DietScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                "Workouts" -> WorkoutScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                "Photos" -> ProgressPhotosScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                "AI Coach" -> ChatbotScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                "Settings" -> SettingsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
