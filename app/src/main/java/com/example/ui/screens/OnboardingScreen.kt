package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    // Temporary form states
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("25") }
    var gender by remember { mutableStateOf("Male") }
    var heightStr by remember { mutableStateOf("175") }
    var weightStr by remember { mutableStateOf("70") }
    
    var activityLevel by remember { mutableStateOf("Moderate") }
    var fitnessGoal by remember { mutableStateOf("Lose Weight") }
    var dietaryPreference by remember { mutableStateOf("Non-Veg") }
    var cuisinePreference by remember { mutableStateOf("Mediterranean") }
    var allergies by remember { mutableStateOf("") }

    val genders = listOf("Male", "Female", "Other")
    val activityLevels = listOf("Sedentary", "Light", "Moderate", "Active")
    val goals = listOf("Lose Weight", "Maintain Weight", "Build Muscle")
    val diets = listOf("Non-Veg", "Veg", "Vegan", "Halal")
    val cuisines = listOf("Mediterranean", "South Asian", "Western", "East Asian")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Indicators
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(color, CircleShape)
                            .size(10.dp)
                    )
                }
            }

            // Horizontal pager for 4 beautiful stages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> OnboardingStepWelcome(
                        name = name,
                        onNameChange = { name = it },
                        ageStr = ageStr,
                        onAgeChange = { ageStr = it },
                        gender = gender,
                        onGenderChange = { gender = it },
                        genders = genders
                    )
                    1 -> OnboardingStepMetrics(
                        heightStr = heightStr,
                        onHeightChange = { heightStr = it },
                        weightStr = weightStr,
                        onWeightChange = { weightStr = it },
                        activityLevel = activityLevel,
                        onActivityLevelChange = { activityLevel = it },
                        activityLevels = activityLevels
                    )
                    2 -> OnboardingStepPreferences(
                        fitnessGoal = fitnessGoal,
                        onGoalChange = { fitnessGoal = it },
                        goals = goals,
                        dietaryPreference = dietaryPreference,
                        onDietChange = { dietaryPreference = it },
                        diets = diets
                    )
                    3 -> OnboardingStepCuisine(
                        cuisinePreference = cuisinePreference,
                        onCuisineChange = { cuisinePreference = it },
                        cuisines = cuisines,
                        allergies = allergies,
                        onAllergiesChange = { allergies = it }
                    )
                }
            }

            // Bottom Navigation Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text("Back", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage < pagerState.pageCount - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                // Complete Onboarding
                                val userProfile = UserProfile(
                                    name = name.ifEmpty { "Fitness Champion" },
                                    age = ageStr.toIntOrNull() ?: 25,
                                    gender = gender,
                                    heightCm = heightStr.toFloatOrNull() ?: 175f,
                                    weightKg = weightStr.toFloatOrNull() ?: 70f,
                                    activityLevel = activityLevel,
                                    fitnessGoal = fitnessGoal,
                                    dietaryPreference = dietaryPreference,
                                    cuisinePreference = cuisinePreference,
                                    allergies = allergies,
                                    isOnboarded = true
                                )
                                onComplete(userProfile)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .testTag("onboarding_next_button")
                        .height(52.dp)
                ) {
                    if (pagerState.currentPage < pagerState.pageCount - 1) {
                        Text("Continue", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                    } else {
                        Text("Get Started", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepWelcome(
    name: String,
    onNameChange: (String) -> Unit,
    ageStr: String,
    onAgeChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    genders: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to FitTrack! 👋",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Let's personalize your fitness and culinary experience in just a few clicks.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("What is your name?") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_name"),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ageStr,
            onValueChange = onAgeChange,
            label = { Text("How old are you?") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Gender Identification",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genders.forEach { g ->
                    val isSelected = g == gender
                    Button(
                        onClick = { onGenderChange(g) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(g)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepMetrics(
    heightStr: String,
    onHeightChange: (String) -> Unit,
    weightStr: String,
    onWeightChange: (String) -> Unit,
    activityLevel: String,
    onActivityLevelChange: (String) -> Unit,
    activityLevels: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Tell us about your body 📏",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = heightStr,
                onValueChange = onHeightChange,
                label = { Text("Height (cm)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = weightStr,
                onValueChange = onWeightChange,
                label = { Text("Weight (kg)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Activity Level",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activityLevels.forEach { level ->
                val isSelected = level == activityLevel
                Card(
                    onClick = { onActivityLevelChange(level) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = level,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val desc = when (level) {
                                "Sedentary" -> "Desk job, little to no exercise"
                                "Light" -> "Light active lifestyle or exercise 1-3 days/week"
                                "Moderate" -> "Moderately active, regular workout 3-5 days/week"
                                else -> "Highly active, heavy athletic training 6-7 days/week"
                            }
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { onActivityLevelChange(level) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepPreferences(
    fitnessGoal: String,
    onGoalChange: (String) -> Unit,
    goals: List<String>,
    dietaryPreference: String,
    onDietChange: (String) -> Unit,
    diets: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Your Personal Goals 🎯",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Fitness Goal",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            goals.forEach { g ->
                val isSelected = g == fitnessGoal
                Button(
                    onClick = { onGoalChange(g) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = g.substringBefore(" "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dietary Preferences",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            diets.forEach { d ->
                val isSelected = d == dietaryPreference
                Button(
                    onClick = { onDietChange(d) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = d, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OnboardingStepCuisine(
    cuisinePreference: String,
    onCuisineChange: (String) -> Unit,
    cuisines: List<String>,
    allergies: String,
    onAllergiesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Authentic Cuisine Choices 🍽️",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Regional Cooking & Cuisine Style",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cuisines.take(2).forEach { c ->
                val isSelected = c == cuisinePreference
                Button(
                    onClick = { onCuisineChange(c) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = c, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cuisines.drop(2).forEach { c ->
                val isSelected = c == cuisinePreference
                Button(
                    onClick = { onCuisineChange(c) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = c, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = allergies,
            onValueChange = onAllergiesChange,
            label = { Text("Food Allergies / Intolerances") },
            placeholder = { Text("e.g. Peanuts, Dairy, Gluten (Leave empty if none)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
