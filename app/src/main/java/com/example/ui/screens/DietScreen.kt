package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Meal
import com.example.data.model.UserProfile
import com.example.ui.FitTrackViewModel
import com.example.ui.theme.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val mealPlanDay by viewModel.todayMealPlan.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isGenerating = viewModel.isDietGenerating

    var showGroceryDialog by remember { mutableStateOf(false) }

    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    val type = remember { Types.newParameterizedType(List::class.java, Meal::class.java) }
    val adapter = remember { moshi.adapter<List<Meal>>(type) }

    val meals = remember(mealPlanDay) {
        val json = mealPlanDay?.mealsJson
        if (json != null) {
            try {
                adapter.fromJson(json) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Auto-generate diet plan on screen load if empty
    LaunchedEffect(Unit) {
        viewModel.generateOrFetchDietPlan(forceRegenerate = false)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("AI Authentic Diet Plan", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { viewModel.generateOrFetchDietPlan(forceRegenerate = true) },
                        enabled = !isGenerating,
                        modifier = Modifier.testTag("diet_refresh_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate Plan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Formulating authentic recipes... 🍳",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (meals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(48.dp))
                    Text("No meal plan generated yet.")
                    Button(onClick = { viewModel.generateOrFetchDietPlan(forceRegenerate = true) }) {
                        Text("Generate Daily Meal Plan")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Diet Summary Stats
                DietMacrosSummary(meals = meals, profile = userProfile ?: UserProfile())

                // Aggregate Grocery list trigger
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    onClick = { showGroceryDialog = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFECFDF5), CircleShape), // Warm pale green
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBasket,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Weekly Grocery Shopping List",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Get required ingredients for your recipes",
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
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = "Daily Menu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // List of 4 meals
                meals.forEach { meal ->
                    MealCard(
                        meal = meal,
                        onEatenToggle = { viewModel.toggleMealEaten(meal.mealType) },
                        onRegenerate = { viewModel.regenerateSingleMeal(meal.mealType) }
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showGroceryDialog) {
        GroceryListDialog(
            meals = meals,
            cuisine = userProfile?.cuisinePreference ?: "Mediterranean",
            onDismiss = { showGroceryDialog = false }
        )
    }
}

@Composable
fun DietMacrosSummary(meals: List<Meal>, profile: UserProfile) {
    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.proteinGrams }
    val totalCarbs = meals.sumOf { it.carbsGrams }
    val totalFat = meals.sumOf { it.fatGrams }
    
    val targetCalories = when (profile.fitnessGoal) {
        "Lose Weight" -> 1600
        "Build Muscle" -> 2500
        else -> 2000
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Target Nutrition Adherence",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "$totalCalories kcal",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Light, letterSpacing = (-1).sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Planned Energy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(CleanSuccessBg, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Goal: $targetCalories kcal",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CleanSuccessText
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            
            // Macro row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroIndicator("Protein", "${totalProtein}g", Color(0xFF60A5FA), Modifier.weight(1f))
                MacroIndicator("Carbs", "${totalCarbs}g", Color(0xFFF59E0B), Modifier.weight(1f))
                MacroIndicator("Fat", "${totalFat}g", Color(0xFFEF4444), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MacroIndicator(label: String, valStr: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(28.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = valStr, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun MealCard(
    meal: Meal,
    onEatenToggle: () -> Unit,
    onRegenerate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (meal.isEaten) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Type + Eaten Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                when (meal.mealType) {
                                    "Breakfast" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                    "Lunch" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    "Dinner" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                                    else -> Color(0xFF9C27B0).copy(alpha = 0.15f)
                                },
                                RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = meal.mealType,
                            color = when (meal.mealType) {
                                "Breakfast" -> Color(0xFFFF9800)
                                "Lunch" -> Color(0xFF4CAF50)
                                "Dinner" -> Color(0xFF2196F3)
                                else -> Color(0xFF9C27B0)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onRegenerate, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Autorenew, contentDescription = "Swap Meal", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Checkbox(
                        checked = meal.isEaten,
                        onCheckedChange = { onEatenToggle() },
                        modifier = Modifier.testTag("meal_eaten_checkbox_${meal.mealType}")
                    )
                }
            }

            // Recipe Name
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textDecoration = if (meal.isEaten) TextDecoration.LineThrough else null,
                color = if (meal.isEaten) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
            )

            // Description
            Text(
                text = meal.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (meal.isEaten) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )

            // Nutrition row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(14.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MealMacro("Protein", "${meal.proteinGrams}g")
                MealMacro("Carbs", "${meal.carbsGrams}g")
                MealMacro("Fat", "${meal.fatGrams}g")
            }
        }
    }
}

@Composable
fun MealMacro(tag: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "$tag:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        Text(text = value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun GroceryListDialog(
    meals: List<Meal>,
    cuisine: String,
    onDismiss: () -> Unit
) {
    // Generate high quality aggregated items depending on selected cuisine
    val groceryItems = remember(meals, cuisine) {
        val list = mutableListOf<String>()
        when (cuisine) {
            "South Asian", "Pakistani" -> {
                list.addAll(listOf("Whole wheat flour (Atta) - 1 kg", "Fresh chicken breast - 500g", "Moong Daal (Split mung beans) - 250g", "Fresh eggs - 1 dozen", "Basmati rice - 1 kg", "Greek Yogurt - 500g", "Fresh spinach, onions, tomatoes", "Ginger & garlic paste, coriander, spices"))
            }
            "Mediterranean" -> {
                list.addAll(listOf("Extra virgin olive oil - 500ml", "Greek Yogurt (0% fat) - 1 kg", "Fresh Salmon or Seabass fillets - 500g", "Quinoa - 500g", "Hummus dip - 1 tub", "Fresh avocado, lemon, baby carrots", "Cherry tomatoes, asparagus spears", "Feta cheese - 250g"))
            }
            else -> {
                list.addAll(listOf("Oatmeal (Steel-cut) - 500g", "Almond milk (Unsweetened) - 1L", "Whey Protein isolate - 1 tub", "Lean turkey or flank steak - 500g", "Fresh sweet potatoes - 1 kg", "Cottage cheese (Low fat) - 500g", "Sourdough bread - 1 loaf", "Fresh peaches, apples, broccoli"))
            }
        }
        list
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Your Grocery Shopping List",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Here are the raw regional ingredients required to cook today's planned meals:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groceryItems.forEach { item ->
                            var checked by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .clickable { checked = !checked }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { checked = it }
                                )
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (checked) TextDecoration.LineThrough else null,
                                    color = if (checked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("grocery_done_button")
                ) {
                    Text("Got It, Thanks!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
