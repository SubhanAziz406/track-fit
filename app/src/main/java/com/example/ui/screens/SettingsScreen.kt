package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.ui.FitTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Local form states
    var name by remember(userProfile) { mutableStateOf(userProfile?.name ?: "") }
    var ageStr by remember(userProfile) { mutableStateOf(userProfile?.age?.toString() ?: "25") }
    var heightStr by remember(userProfile) { mutableStateOf(userProfile?.heightCm?.toInt()?.toString() ?: "175") }
    var weightStr by remember(userProfile) { mutableStateOf(userProfile?.weightKg?.toInt()?.toString() ?: "70") }
    var apiKey by remember(userProfile) { mutableStateOf(userProfile?.aiApiKey ?: "") }
    
    var fitnessGoal by remember(userProfile) { mutableStateOf(userProfile?.fitnessGoal ?: "Lose Weight") }
    var cuisinePreference by remember(userProfile) { mutableStateOf(userProfile?.cuisinePreference ?: "Mediterranean") }
    var activityLevel by remember(userProfile) { mutableStateOf(userProfile?.activityLevel ?: "Moderate") }

    // Backup dialog states
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var backupJsonText by remember { mutableStateOf("") }
    var restoreJsonText by remember { mutableStateOf("") }

    // Toggle reminder states
    var dailyReminderEnabled by remember { mutableStateOf(true) }
    var workoutReminderEnabled by remember { mutableStateOf(true) }
    var hydrationReminderEnabled by remember { mutableStateOf(true) }

    val goals = listOf("Lose Weight", "Maintain Weight", "Build Muscle")
    val cuisines = listOf("Mediterranean", "South Asian", "Western", "East Asian")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Demographic Details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("User Profile Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_profile_name")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text("Age") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it },
                            label = { Text("Height (cm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Save Profile details button
                    Button(
                        onClick = {
                            val prof = userProfile ?: UserProfile()
                            viewModel.saveProfile(
                                prof.copy(
                                    name = name,
                                    age = ageStr.toIntOrNull() ?: prof.age,
                                    heightCm = heightStr.toFloatOrNull() ?: prof.heightCm,
                                    weightKg = weightStr.toFloatOrNull() ?: prof.weightKg,
                                    fitnessGoal = fitnessGoal,
                                    cuisinePreference = cuisinePreference,
                                    aiApiKey = apiKey
                                )
                            )
                            Toast.makeText(context, "Profile Saved Successfully! 💾", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_profile_info"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bring Your Own Key configuration card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Bring-Your-Own API Key (Optional)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = "FitTrack runs completely offline out-of-the-box using gourmet templates. Paste in your own Gemini API key below to enable live, conversational fitness coaching advice!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Gemini API Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_api_key_input")
                    )
                }
            }

            // Data backups & Restore buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Data Ownership & Backups", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "Because your fitness timeline, metrics, and diet plan adherence are stored 100% locally on this device, we highly recommend exporting occasional backups to prevent data loss.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBackupString { json ->
                                    if (json.isNotEmpty()) {
                                        backupJsonText = json
                                        clipboardManager.setText(AnnotatedString(json))
                                        showBackupDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("export_backup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON")
                        }

                        Button(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.weight(1f).testTag("import_backup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON")
                        }
                    }
                }
            }

            // Reminders toggle list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Notifications & Reminders", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    
                    ReminderRow(title = "Daily wellness check-in nudge", enabled = dailyReminderEnabled, onToggle = { dailyReminderEnabled = it })
                    ReminderRow(title = "Workout session reminders", enabled = workoutReminderEnabled, onToggle = { workoutReminderEnabled = it })
                    ReminderRow(title = "Hydration tracking reminders", enabled = hydrationReminderEnabled, onToggle = { hydrationReminderEnabled = it })
                }
            }

            // Privacy disclosure note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "FitTrack Privacy Guarantee: None of your photos, weights, or nutrition metrics are ever transmitted to a server without your explicit command.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Export Backup Dialog
    if (showBackupDialog) {
        Dialog(onDismissRequest = { showBackupDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Backup Data Exported! 🎉", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Your backup string has been copied to your clipboard. Paste and store this text securely in a safe place:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = backupJsonText.take(500) + if (backupJsonText.length > 500) "... [Truncated]" else "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }

                    Button(
                        onClick = { showBackupDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    // Import Restore Dialog
    if (showRestoreDialog) {
        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Import Backup Data ☁️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Paste in your copied backup JSON text block below to fully restore your FitTrack profile and progress history:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                    
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        placeholder = { Text("Paste JSON string here...") },
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp).testTag("import_backup_input_text")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                viewModel.importBackupString(restoreJsonText) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Backup Restored Successfully! 🚀", Toast.LENGTH_SHORT).show()
                                        showRestoreDialog = false
                                    } else {
                                        Toast.makeText(context, "Restoring failed. Check JSON format.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("import_backup_confirm_button")
                        ) {
                            Text("Confirm Restore")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderRow(title: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}
