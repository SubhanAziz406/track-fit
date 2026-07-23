package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.Date
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DailyLog
import com.example.data.model.UserProfile
import com.example.ui.FitTrackViewModel
import com.example.ui.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    viewModel: FitTrackViewModel,
    onNavigateToWorkouts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayLog by viewModel.todayLog.collectAsState()
    val allLogs by viewModel.allDailyLogs.collectAsState()
    val workouts by viewModel.allWorkoutLogs.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    var showCheckInDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header & Streak Display
        DashboardHeader(profile = userProfile, allLogs = allLogs, onCheckInClick = { showCheckInDialog = true })

        // Quick Tracker Row (Water & Sleep widgets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WaterWidget(
                currentWaterMl = todayLog?.waterIntakeMl ?: 0,
                onAddWater = { ml -> viewModel.addWater(ml) },
                modifier = Modifier.weight(1f)
            )
            SleepWidget(
                sleepHours = todayLog?.sleepHours ?: 0f,
                modifier = Modifier.weight(1f)
            )
        }

        // Active Workout Shortcut Card
        WorkoutShortcutCard(
            workoutsCompleted = workouts.size,
            onNavigate = onNavigateToWorkouts
        )

        // Progress Weight Line Chart
        WeightTrendCard(allLogs = allLogs)

        // Water Intake Bar Chart
        WaterTrendCard(allLogs = allLogs)

        // Achievements & Badges
        AchievementsRow(allLogs = allLogs, workoutsCompleted = workouts.size)
        
        Spacer(modifier = Modifier.height(60.dp)) // Nav bar buffer
    }

    if (showCheckInDialog) {
        CheckInDialog(
            currentLog = todayLog,
            onDismiss = { showCheckInDialog = false },
            onSave = { weight, waist, chest, arms, thighs, mood, energy, water, sleep ->
                viewModel.saveCheckIn(weight, waist, chest, arms, thighs, mood, energy, water, sleep)
                showCheckInDialog = false
            }
        )
    }
}

@Composable
fun DashboardHeader(
    profile: UserProfile?,
    allLogs: List<DailyLog>,
    onCheckInClick: () -> Unit
) {
    // Simple streak calculation
    val streakCount = remember(allLogs) {
        var streak = 0
        val sortedLogs = allLogs.sortedByDescending { it.dateString }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        var checkDate = Date()
        for (log in sortedLogs) {
            val logDate = sdf.parse(log.dateString) ?: continue
            val diffMs = checkDate.time - logDate.time
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
            
            if (diffDays == 0 || diffDays == 1) {
                streak++
                checkDate = logDate
            } else if (diffDays > 1) {
                break
            }
        }
        streak
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top welcome bar (as in the HTML header)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GOOD MORNING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Hey, ${profile?.name?.takeIf { it.isNotBlank() } ?: "Sarah"}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Custom modern gradient avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF60A5FA), Color(0xFF6366F1))
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Streak Card / Daily Wellness Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFFFEDD5), CircleShape), // Warm orange background
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "$streakCount Day Streak",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (streakCount > 0) "Keep the momentum going! 💪" else "Start your check-in today!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Button(
                    onClick = onCheckInClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .testTag("daily_check_in_button")
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Check In", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun WaterWidget(
    currentWaterMl: Int,
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(156.dp),
        colors = CardDefaults.cardColors(containerColor = HydrationCardBg),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.6f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = "Water",
                        tint = HydrationCardText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "HYDRATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = HydrationCardText.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$currentWaterMl ml",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = HydrationCardText
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAddWater(250) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("add_water_250"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        "+250ml",
                        color = HydrationCardText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = { onAddWater(500) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        "+500ml",
                        color = HydrationCardText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SleepWidget(
    sleepHours: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(156.dp),
        colors = CardDefaults.cardColors(containerColor = SleepCardBg),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.6f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Sleep",
                        tint = SleepCardText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "SLEEP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = SleepCardText.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format("%.1f", sleepHours)}h",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = SleepCardText
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val progress = (sleepHours / 8f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = SleepCardText,
                    trackColor = Color.White.copy(alpha = 0.5f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = if (sleepHours >= 7f) "Healthy rest! 🌙" else "Aim for 7-8h",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = SleepCardText.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun WorkoutShortcutCard(
    workoutsCompleted: Int,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S PLAN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = "View Routine",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFFEDD5), RoundedCornerShape(14.dp)), // Light orange
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏋️", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "Active Workout Routines",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$workoutsCompleted Completed • Tap to start session",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeightTrendCard(allLogs: List<DailyLog>) {
    val weightLogs = remember(allLogs) {
        allLogs.sortedBy { it.dateString }.filter { it.weightKg != null }.takeLast(7)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Progress",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .background(CleanSuccessBg, RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Active Tracking",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CleanSuccessText
                    )
                }
            }
            
            val lastWeight = weightLogs.lastOrNull()?.weightKg
            if (lastWeight != null) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "$lastWeight",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (weightLogs.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log your weight on 2+ days to see a trend chart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                val weights = weightLogs.mapNotNull { it.weightKg }
                val maxWeight = (weights.maxOrNull() ?: 100f) + 2f
                val minWeight = (weights.minOrNull() ?: 0f) - 2f
                val weightRange = maxWeight - minWeight

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val spacingX = width / (weights.size - 1)

                    val points = weights.mapIndexed { idx, w ->
                        val x = idx * spacingX
                        val ratio = (w - minWeight) / weightRange
                        val y = height - (ratio * height)
                        Offset(x, y)
                    }

                    // Draw gradient area under curve
                    val fillPath = Path().apply {
                        moveTo(0f, height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CleanPrimary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw connection line
                    val strokePath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = strokePath,
                        color = CleanPrimary,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw dots on weight entries
                    points.forEach { point ->
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = CleanPrimary,
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${weights.firstOrNull() ?: ""}kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Time trend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${weights.lastOrNull() ?: ""}kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun WaterTrendCard(allLogs: List<DailyLog>) {
    val waterLogs = remember(allLogs) {
        allLogs.sortedBy { it.dateString }.takeLast(7)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Water Intake Trend",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (waterLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No water records logged yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                val waters = waterLogs.map { it.waterIntakeMl }
                val maxWater = (waters.maxOrNull() ?: 2000).coerceAtLeast(2000)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    
                    val barCount = waters.size
                    val spacing = 16.dp.toPx()
                    val totalSpacing = spacing * (barCount - 1)
                    val barWidth = (width - totalSpacing) / barCount

                    waters.forEachIndexed { idx, waterVal ->
                        val barHeight = (waterVal.toFloat() / maxWater.toFloat()) * height
                        val x = idx * (barWidth + spacing)
                        val y = height - barHeight
                        
                        drawRoundRect(
                            color = CleanPrimary.copy(alpha = 0.85f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    waterLogs.forEach { log ->
                        val label = log.dateString.substringAfterLast("-")
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsRow(allLogs: List<DailyLog>, workoutsCompleted: Int) {
    Column {
        Text(
            text = "Your Badges & Achievements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BadgeItem(
                title = "Hydrator",
                desc = "Logged water",
                icon = Icons.Default.LocalDrink,
                color = MaterialTheme.colorScheme.secondary,
                unlocked = allLogs.any { it.waterIntakeMl > 0 }
            )
            BadgeItem(
                title = "Early Bird",
                desc = "Logged sleep",
                icon = Icons.Default.Bedtime,
                color = MaterialTheme.colorScheme.tertiary,
                unlocked = allLogs.any { it.sleepHours > 0f }
            )
            BadgeItem(
                title = "Iron Lifter",
                desc = "Completed 1st workout",
                icon = Icons.Default.FitnessCenter,
                color = MaterialTheme.colorScheme.primary,
                unlocked = workoutsCompleted >= 1
            )
            BadgeItem(
                title = "Dedicated",
                desc = "Completed 10 workouts",
                icon = Icons.Default.Star,
                color = Color(0xFFFFD700),
                unlocked = workoutsCompleted >= 10
            )
        }
    }
}

@Composable
fun BadgeItem(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    unlocked: Boolean
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (unlocked) color.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (unlocked) color else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDialog(
    currentLog: DailyLog?,
    onDismiss: () -> Unit,
    onSave: (
        weight: Float?,
        waist: Float?,
        chest: Float?,
        arms: Float?,
        thighs: Float?,
        mood: String,
        energy: Int,
        water: Int,
        sleep: Float
    ) -> Unit
) {
    var weightStr by remember { mutableStateOf(currentLog?.weightKg?.toString() ?: "") }
    var waistStr by remember { mutableStateOf(currentLog?.waistCm?.toString() ?: "") }
    var chestStr by remember { mutableStateOf(currentLog?.chestCm?.toString() ?: "") }
    var armsStr by remember { mutableStateOf(currentLog?.armsCm?.toString() ?: "") }
    var thighsStr by remember { mutableStateOf(currentLog?.thighsCm?.toString() ?: "") }
    
    var moodSelected by remember { mutableStateOf(currentLog?.mood ?: "Good") }
    var energySelected by remember { mutableStateOf(currentLog?.energyLevel ?: 3) }
    var waterStr by remember { mutableStateOf(currentLog?.waterIntakeMl?.toString() ?: "0") }
    var sleepStr by remember { mutableStateOf(currentLog?.sleepHours?.toString() ?: "7.0") }

    val moods = listOf("Great", "Good", "Neutral", "Tired", "Stressed")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Daily Wellness Check-In",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Weight Input
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("check_in_weight")
                )

                // Water & Sleep Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = waterStr,
                        onValueChange = { waterStr = it },
                        label = { Text("Water (ml)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sleepStr,
                        onValueChange = { sleepStr = it },
                        label = { Text("Sleep (hours)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Optional measurements heading
                Text(
                    text = "Body Measurements (Optional)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = waistStr,
                        onValueChange = { waistStr = it },
                        label = { Text("Waist (cm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = chestStr,
                        onValueChange = { chestStr = it },
                        label = { Text("Chest (cm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = armsStr,
                        onValueChange = { armsStr = it },
                        label = { Text("Arms (cm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = thighsStr,
                        onValueChange = { thighsStr = it },
                        label = { Text("Thighs (cm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Mood selector
                Text(
                    text = "Current Mood",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { m ->
                        val isSelected = m == moodSelected
                        FilterChip(
                            selected = isSelected,
                            onClick = { moodSelected = m },
                            label = { Text(m) }
                        )
                    }
                }

                // Energy level slider
                Text(
                    text = "Energy Level: $energySelected / 5",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = energySelected.toFloat(),
                    onValueChange = { energySelected = it.roundToInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val w = weightStr.toFloatOrNull()
                            val wa = waistStr.toFloatOrNull()
                            val ch = chestStr.toFloatOrNull()
                            val ar = armsStr.toFloatOrNull()
                            val th = thighsStr.toFloatOrNull()
                            val wat = waterStr.toIntOrNull() ?: 0
                            val sl = sleepStr.toFloatOrNull() ?: 7f
                            onSave(w, wa, ch, ar, th, moodSelected, energySelected, wat, sl)
                        },
                        modifier = Modifier.testTag("save_check_in_button")
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
