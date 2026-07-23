package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExerciseSetLog
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutSet
import com.example.ui.FitTrackViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.math.roundToInt

@Composable
fun WorkoutScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val isWorkoutActive = viewModel.isWorkoutActive
    val workoutLogs by viewModel.allWorkoutLogs.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isWorkoutActive) {
            // Running active workout UI
            ActiveWorkoutView(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            // Standard Gym/Home selection & Preset list + past logs UI
            WorkoutDirectoryView(
                viewModel = viewModel,
                pastLogs = workoutLogs,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun WorkoutDirectoryView(
    viewModel: FitTrackViewModel,
    pastLogs: List<WorkoutLog>,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf("Gym") } // Gym, Home
    var showLogsHistory by remember { mutableStateOf(false) }

    val presetGymRoutines = listOf(
        RoutineTemplate(
            title = "Push Strength Day",
            muscleGroup = "Push / Chest & Shoulders",
            durationEstMin = 45,
            exercises = listOf(
                ExerciseTemplate("Barbell Bench Press", "Push", 3, 10, 60f, 60),
                ExerciseTemplate("Overhead Shoulder Press", "Push", 3, 10, 40f, 60),
                ExerciseTemplate("Incline Dumbbell Flyes", "Push", 3, 12, 14f, 60),
                ExerciseTemplate("Tricep Pushdowns", "Push", 3, 12, 20f, 45)
            )
        ),
        RoutineTemplate(
            title = "Pull Hypertrophy Day",
            muscleGroup = "Pull / Back & Biceps",
            durationEstMin = 45,
            exercises = listOf(
                ExerciseTemplate("Barbell Deadlifts", "Pull", 3, 5, 80f, 90),
                ExerciseTemplate("Lat Pulldowns", "Pull", 3, 10, 45f, 60),
                ExerciseTemplate("Seated Cable Rows", "Pull", 3, 12, 40f, 60),
                ExerciseTemplate("Dumbbell Bicep Curls", "Pull", 3, 12, 12f, 45)
            )
        ),
        RoutineTemplate(
            title = "Lower Body Squat Day",
            muscleGroup = "Legs / Quads & Calves",
            durationEstMin = 50,
            exercises = listOf(
                ExerciseTemplate("Barbell Squats", "Legs", 4, 8, 70f, 90),
                ExerciseTemplate("Romanian Deadlifts", "Legs", 3, 10, 50f, 60),
                ExerciseTemplate("Leg Extensions", "Legs", 3, 12, 30f, 45),
                ExerciseTemplate("Standing Calf Raises", "Legs", 4, 15, 40f, 45)
            )
        )
    )

    val presetHomeRoutines = listOf(
        RoutineTemplate(
            title = "Bodyweight HIIT Blast",
            muscleGroup = "Full Body Endurance",
            durationEstMin = 30,
            exercises = listOf(
                ExerciseTemplate("Air Squats", "Legs", 3, 20, 0f, 45),
                ExerciseTemplate("Standard Pushups", "Push", 3, 15, 0f, 45),
                ExerciseTemplate("Mountain Climbers", "Core", 3, 30, 0f, 30),
                ExerciseTemplate("Jumping Jacks", "Cardio", 3, 40, 0f, 30)
            )
        ),
        RoutineTemplate(
            title = "Core Sculpt & Stability",
            muscleGroup = "Abs & Obliques",
            durationEstMin = 20,
            exercises = listOf(
                ExerciseTemplate("Ab Crunches", "Core", 3, 20, 0f, 30),
                ExerciseTemplate("Lying Leg Raises", "Core", 3, 12, 0f, 30),
                ExerciseTemplate("Russian Twists", "Core", 3, 24, 0f, 30),
                ExerciseTemplate("Forearm Plank Hold", "Core", 3, 45, 0f, 30)
            )
        ),
        RoutineTemplate(
            title = "Aerobic Cardio Burner",
            muscleGroup = "Cardio & Stamina",
            durationEstMin = 25,
            exercises = listOf(
                ExerciseTemplate("Jumping Burpees", "Cardio", 3, 10, 0f, 45),
                ExerciseTemplate("High Knees Jog", "Cardio", 3, 30, 0f, 30),
                ExerciseTemplate("Plank Walkouts", "Cardio", 3, 10, 0f, 45),
                ExerciseTemplate("Plank Jacks", "Cardio", 3, 25, 0f, 30)
            )
        )
    )

    val activePresets = if (selectedMode == "Gym") presetGymRoutines else presetHomeRoutines

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle view type (Presets vs Past logs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showLogsHistory) "Workout Logs History" else "Workout Sessions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = { showLogsHistory = !showLogsHistory },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (showLogsHistory) Icons.Default.FitnessCenter else Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(if (showLogsHistory) "Show Presets" else "Show History")
                }
            }
        }

        if (showLogsHistory) {
            if (pastLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No past workouts recorded yet. Start sweating! 💦")
                }
            } else {
                pastLogs.forEach { log ->
                    PastWorkoutLogCard(log = log, onDelete = { viewModel.deleteWorkoutLog(log.id) })
                }
            }
        } else {
            // Gym / Home Mode Selector Tabs
            TabRow(
                selectedTabIndex = if (selectedMode == "Gym") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedMode == "Gym",
                    onClick = { selectedMode = "Gym" },
                    modifier = Modifier.testTag("mode_gym")
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Gym (Equipped)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Tab(
                    selected = selectedMode == "Home",
                    onClick = { selectedMode = "Home" },
                    modifier = Modifier.testTag("mode_home")
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Home (Bodyweight)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text(
                text = "Preset Routine Templates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Dynamic Preset Routines list
            activePresets.forEach { preset ->
                PresetRoutineCard(
                    preset = preset,
                    onStart = {
                        // Map presets to dynamic log entries
                        val logs = preset.exercises.map { ext ->
                            ExerciseSetLog(
                                exerciseName = ext.name,
                                category = ext.category,
                                sets = List(ext.setCount) { idx ->
                                    WorkoutSet(setNumber = idx + 1, reps = ext.reps, weightKg = ext.weight, isCompleted = false)
                                }
                            )
                        }
                        viewModel.startWorkout(preset.title, selectedMode, logs)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun PastWorkoutLogCard(log: WorkoutLog, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    val type = remember { Types.newParameterizedType(List::class.java, ExerciseSetLog::class.java) }
    val adapter = remember { moshi.adapter<List<ExerciseSetLog>>(type) }
    
    val exercises = remember(log.exercisesJson) {
        try {
            adapter.fromJson(log.exercisesJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = log.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "${log.dateString} • ${log.workoutMode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(100.dp)) // Nice soft red
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "${log.caloriesBurned} kcal", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(text = "${log.durationMinutes} mins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(text = "${exercises.size} Exercises", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    exercises.forEach { ex ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = ex.exerciseName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ex.sets.forEach { set ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (set.isCompleted) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${set.reps}r @ ${set.weightKg}kg",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (set.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetRoutineCard(
    preset: RoutineTemplate,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = preset.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(text = preset.muscleGroup, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "~${preset.durationEstMin} Min", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                preset.exercises.forEach { ex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• ${ex.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Text(text = "${ex.setCount} sets x ${ex.reps} reps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_preset_${preset.title.replace(" ", "_")}"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Routine Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveWorkoutView(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val routineTitle = viewModel.activeWorkoutRoutine ?: "Workout Session"
    val mode = viewModel.activeWorkoutMode
    val durationSec = viewModel.workoutDurationSeconds
    val exercises = viewModel.activeExercises
    
    val restTimeTotal = viewModel.restTimeTotalSeconds
    val restTimeRem = viewModel.restTimeRemainingSeconds
    val isRestActive = viewModel.isRestTimerRunning

    val durationStr = remember(durationSec) {
        val mins = durationSec / 60
        val secs = durationSec % 60
        String.format("%02d:%02d", mins, secs)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ticking Header Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = routineTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "Active $mode session", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(text = durationStr, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Rest timer notification card
        AnimatedVisibility(visible = isRestActive) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text(text = "Rest Timer active. Chill...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Text(
                        text = "${restTimeRem}s remaining",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Active list of exercises
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exercises.size) { exIdx ->
                val ex = exercises[exIdx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = ex.exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Headings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Set", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                            Text(text = "Weight (kg)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                            Text(text = "Reps", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                            Text(text = "Done", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
                        }

                        ex.sets.forEachIndexed { setIdx, set ->
                            var repVal by remember { mutableStateOf(set.reps.toString()) }
                            var weightVal by remember { mutableStateOf(set.weightKg.roundToInt().toString()) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Set Label
                                Text(
                                    text = "${set.setNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )

                                // Weight Input
                                OutlinedTextField(
                                    value = weightVal,
                                    onValueChange = {
                                        weightVal = it
                                        val w = it.toFloatOrNull() ?: 0f
                                        val r = repVal.toIntOrNull() ?: 0
                                        viewModel.updateWorkoutSet(exIdx, setIdx, r, w, set.isCompleted)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(48.dp)
                                        .testTag("weight_input_${exIdx}_${setIdx}"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )

                                // Reps Input
                                OutlinedTextField(
                                    value = repVal,
                                    onValueChange = {
                                        repVal = it
                                        val r = it.toIntOrNull() ?: 0
                                        val w = weightVal.toFloatOrNull() ?: 0f
                                        viewModel.updateWorkoutSet(exIdx, setIdx, r, w, set.isCompleted)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(48.dp)
                                        .testTag("reps_input_${exIdx}_${setIdx}"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )

                                // Complete Checkbox
                                Checkbox(
                                    checked = set.isCompleted,
                                    onCheckedChange = { completed ->
                                        val r = repVal.toIntOrNull() ?: set.reps
                                        val w = weightVal.toFloatOrNull() ?: set.weightKg
                                        viewModel.updateWorkoutSet(exIdx, setIdx, r, w, completed)
                                        
                                        // Auto-trigger 60s rest timer if completed!
                                        if (completed) {
                                            viewModel.triggerRestTimer(60)
                                        }
                                    },
                                    modifier = Modifier.testTag("set_checkbox_${exIdx}_${setIdx}")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Complete / Cancel Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.cancelActiveWorkout() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Quit Workout", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.finishWorkout() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("finish_workout_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Complete Session", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Data structures
data class RoutineTemplate(
    val title: String,
    val muscleGroup: String,
    val durationEstMin: Int,
    val exercises: List<ExerciseTemplate>
)

data class ExerciseTemplate(
    val name: String,
    val category: String,
    val setCount: Int,
    val reps: Int,
    val weight: Float,
    val restSeconds: Int
)
