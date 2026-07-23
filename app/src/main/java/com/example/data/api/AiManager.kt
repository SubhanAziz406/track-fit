package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DailyLog
import com.example.data.model.Meal
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutLog
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object AiManager {
    private const val TAG = "AiManager"
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Resolves the API key to use. If userProfile has a custom key, use it.
     * Otherwise fall back to BuildConfig.GEMINI_API_KEY.
     */
    private fun getApiKey(profile: UserProfile?): String {
        val userKey = profile?.aiApiKey?.trim() ?: ""
        if (userKey.isNotEmpty()) {
            return userKey
        }
        return BuildConfig.GEMINI_API_KEY
    }

    /**
     * Generates a personalized daily diet plan.
     */
    suspend fun generateDietPlan(profile: UserProfile): List<Meal> {
        val apiKey = getApiKey(profile)
        
        // If no API key is provided, or if it is the placeholder, use the offline fallback
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            Log.d(TAG, "No valid Gemini API key. Using regional offline fallback.")
            return getOfflineFallbackMeals(profile)
        }

        val prompt = """
            You are an expert personalized sports nutritionist and professional chef.
            Generate a structured, regionally authentic 1-day personalized meal plan in JSON format based on the following profile:
            - Age: ${profile.age}
            - Gender: ${profile.gender}
            - Height: ${profile.heightCm} cm
            - Weight: ${profile.weightKg} kg
            - Activity Level: ${profile.activityLevel}
            - Goal: ${profile.fitnessGoal}
            - Dietary Preference: ${profile.dietaryPreference}
            - Cuisine Preference: ${profile.cuisinePreference}
            - Allergies: ${profile.allergies.ifEmpty { "None" }}

            The response MUST be a valid JSON array of meal objects, containing exactly four meals: Breakfast, Lunch, Dinner, Snack.
            Each meal object in the JSON array MUST have the exact following fields:
            - 'mealType': 'Breakfast' or 'Lunch' or 'Dinner' or 'Snack'
            - 'name': String (creative, appetizing name matching the selected regional style: ${profile.cuisinePreference})
            - 'description': String (brief description including ingredients and preparation)
            - 'calories': Integer
            - 'proteinGrams': Integer
            - 'carbsGrams': Integer
            - 'fatGrams': Integer

            Ensure the calories and macros are tailored to the profile's daily nutritional goals.
            Return ONLY the raw JSON array. Do not include markdown code block characters like ```json or any other text before or after the JSON.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.3f)
        )

        return try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (jsonText != null) {
                // Extract clean JSON block if model still wrapped it in markdown
                val cleanJson = extractJson(jsonText)
                val type = Types.newParameterizedType(List::class.java, Meal::class.java)
                val adapter = moshi.adapter<List<Meal>>(type)
                adapter.fromJson(cleanJson) ?: getOfflineFallbackMeals(profile)
            } else {
                getOfflineFallbackMeals(profile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API failed: ${e.message}", e)
            getOfflineFallbackMeals(profile)
        }
    }

    /**
     * Helper to clean up any markdown response wrapping.
     */
    private fun extractJson(text: String): String {
        var clean = text.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substringAfter("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.substringBeforeLast("```")
        }
        return clean.trim()
    }

    /**
     * Chatbot advice with context of the user profile and recent logs/workouts.
     */
    suspend fun getChatbotResponse(
        userMessage: String,
        profile: UserProfile,
        recentLogs: List<DailyLog>,
        recentWorkouts: List<WorkoutLog>
    ): String {
        val apiKey = getApiKey(profile)
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            return getOfflineChatbotResponse(userMessage, profile, recentLogs, recentWorkouts)
        }

        // Prepare context
        val contextPrompt = """
            You are FitCoach, a friendly, professional AI Personal Trainer and Sports Dietitian.
            The user profile is:
            - Name: ${profile.name.ifEmpty { "User" }}
            - Age: ${profile.age}, Gender: ${profile.gender}
            - Height: ${profile.heightCm} cm, Current Weight: ${profile.weightKg} kg
            - Activity Level: ${profile.activityLevel}, Goal: ${profile.fitnessGoal}
            - Diet: ${profile.dietaryPreference}, Cuisine Pref: ${profile.cuisinePreference}
            
            Recent Progress Logs (Last ${recentLogs.size} entries):
            ${recentLogs.joinToString("\n") { 
                "- Date: ${it.dateString}, Weight: ${it.weightKg ?: "Not logged"} kg, Mood: ${it.mood}, Water: ${it.waterIntakeMl}ml, Sleep: ${it.sleepHours} hours" 
            }}
            
            Recent Workouts Logged:
            ${recentWorkouts.take(5).joinToString("\n") {
                "- Date: ${it.dateString}, Routine: ${it.title}, Mode: ${it.workoutMode}, Dur: ${it.durationMinutes}m, Calories: ${it.caloriesBurned} kcal"
            }}

            Provide coaching advice, motivation, nutritional feedback, or exercise suggestions. Be concise, encouraging, and direct. Avoid generic disclaimers where possible unless medically critical.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = userMessage)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = contextPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I'm having trouble formulating a response right now. Please try again!"
        } catch (e: Exception) {
            Log.e(TAG, "Chatbot generation failed: ${e.message}", e)
            "Error: ${e.localizedMessage}. Please verify your API key in settings or try again."
        }
    }

    /**
     * High-quality regional meal templates for offline fallback/prototyping.
     */
    private fun getOfflineFallbackMeals(profile: UserProfile): List<Meal> {
        val targetCalories = when (profile.fitnessGoal) {
            "Lose Weight" -> 1600
            "Build Muscle" -> 2500
            else -> 2000
        }

        return when (profile.cuisinePreference) {
            "South Asian", "Pakistani" -> listOf(
                Meal(
                    mealType = "Breakfast",
                    name = "Masala Egg Omelette & Whole Wheat Roti",
                    description = "2 eggs whisked with green chilies, onions, coriander, cooked with a touch of olive oil, served with one whole wheat chapati/roti.",
                    calories = (targetCalories * 0.25).toInt(),
                    proteinGrams = 22,
                    carbsGrams = 35,
                    fatGrams = 12
                ),
                Meal(
                    mealType = "Lunch",
                    name = "Regional Spiced Chicken Biryani (Low Oil)",
                    description = "Basmati rice cooked with steamed spiced chicken breast, mint, yogurt, saffron, and traditional spices. Served with a cucumber raita.",
                    calories = (targetCalories * 0.35).toInt(),
                    proteinGrams = 38,
                    carbsGrams = 60,
                    fatGrams = 10
                ),
                Meal(
                    mealType = "Dinner",
                    name = "Tandoori Fish Skewers & Moong Daal Soup",
                    description = "Marinated grilled tandoori white fish skewered with bell peppers and onions, served with a cup of light yellow split-lentils (moong daal) broth.",
                    calories = (targetCalories * 0.28).toInt(),
                    proteinGrams = 32,
                    carbsGrams = 28,
                    fatGrams = 8
                ),
                Meal(
                    mealType = "Snack",
                    name = "Spiced Chickpea Chaat & Green Tea",
                    description = "Boiled chickpeas tossed with chopped cucumbers, tomatoes, lemon juice, and a pinch of chaat masala. Served with hot cardamom green tea.",
                    calories = (targetCalories * 0.12).toInt(),
                    proteinGrams = 8,
                    carbsGrams = 22,
                    fatGrams = 2
                )
            )
            "Mediterranean" -> listOf(
                Meal(
                    mealType = "Breakfast",
                    name = "Greek Yogurt Bowl with Honey & Walnuts",
                    description = "High-protein Greek yogurt topped with a handful of crushed walnuts, fresh blueberries, and a light drizzle of organic honey.",
                    calories = (targetCalories * 0.25).toInt(),
                    proteinGrams = 24,
                    carbsGrams = 30,
                    fatGrams = 10
                ),
                Meal(
                    mealType = "Lunch",
                    name = "Grilled Lemon Herb Chicken Quinoa Salad",
                    description = "Grilled skinless chicken breast sliced over cooked quinoa, cherry tomatoes, cucumbers, Kalamata olives, and crumbled feta, lightly dressed with olive oil and lemon juice.",
                    calories = (targetCalories * 0.35).toInt(),
                    proteinGrams = 42,
                    carbsGrams = 45,
                    fatGrams = 12
                ),
                Meal(
                    mealType = "Dinner",
                    name = "Baked Salmon with Roasted Asparagus",
                    description = "Salmon fillet baked with dill and garlic, accompanied by roasted asparagus spears and a small sweet potato mash.",
                    calories = (targetCalories * 0.28).toInt(),
                    proteinGrams = 35,
                    carbsGrams = 24,
                    fatGrams = 14
                ),
                Meal(
                    mealType = "Snack",
                    name = "Hummus & Carrot Sticks",
                    description = "3 tablespoons of classic sesame-chickpea hummus paired with baby carrots and cucumber slices.",
                    calories = (targetCalories * 0.12).toInt(),
                    proteinGrams = 6,
                    carbsGrams = 15,
                    fatGrams = 5
                )
            )
            else -> listOf( // Western/Default Clean
                Meal(
                    mealType = "Breakfast",
                    name = "Avocado Toast & Scrambled Eggs",
                    description = "One slice of artisanal whole-grain sourdough toast spread with mashed avocado, served alongside two soft scrambled egg whites and one whole egg.",
                    calories = (targetCalories * 0.25).toInt(),
                    proteinGrams = 20,
                    carbsGrams = 28,
                    fatGrams = 12
                ),
                Meal(
                    mealType = "Lunch",
                    name = "Turkey Breast & Sweet Potato Bowl",
                    description = "Oven-roasted lean turkey breast slices served with cubed steamed sweet potatoes and steamed broccoli florets.",
                    calories = (targetCalories * 0.35).toInt(),
                    proteinGrams = 40,
                    carbsGrams = 50,
                    fatGrams = 8
                ),
                Meal(
                    mealType = "Dinner",
                    name = "Lean Flank Steak with Garlic Green Beans",
                    description = "Grilled lean flank steak served alongside skillet-seared green beans seasoned with minced garlic and a touch of olive oil.",
                    calories = (targetCalories * 0.28).toInt(),
                    proteinGrams = 36,
                    carbsGrams = 18,
                    fatGrams = 10
                ),
                Meal(
                    mealType = "Snack",
                    name = "Whey Protein Shake & Almonds",
                    description = "One scoop of premium whey isolate blended with water, paired with 10 whole raw almonds.",
                    calories = (targetCalories * 0.12).toInt(),
                    proteinGrams = 28,
                    carbsGrams = 6,
                    fatGrams = 6
                )
            )
        }
    }

    /**
     * Offline rules-engine fallback chatbot responses to feel authentic and high quality.
     */
    private fun getOfflineChatbotResponse(
        message: String,
        profile: UserProfile,
        logs: List<DailyLog>,
        workouts: List<WorkoutLog>
    ): String {
        val msg = message.lowercase()
        val userName = profile.name.ifEmpty { "Champion" }
        
        return when {
            msg.contains("weight") || msg.contains("loss") || msg.contains("lose") -> {
                val lastWeight = logs.firstOrNull { it.weightKg != null }?.weightKg ?: profile.weightKg
                "Hey $userName! Sticking to a consistent routine is key. Your profile weight is set to $lastWeight kg. To reach your goal of '${profile.fitnessGoal}', aim for a daily calorie intake of around ${if (profile.fitnessGoal == "Lose Weight") "1600" else "2200"} kcal. Try tracking your water intake too—staying hydrated speeds up your metabolism!"
            }
            msg.contains("workout") || msg.contains("exercise") || msg.contains("routine") -> {
                val count = workouts.size
                "Great question! You've logged $count total workout sessions so far. For active goals, balancing Gym-based resistance sessions (with weights) and Home-based high-intensity interval training is highly effective. Remember to log your sets, reps, and weights to see your strength trends over time!"
            }
            msg.contains("diet") || msg.contains("meal") || msg.contains("food") || msg.contains("recipe") -> {
                "For your '${profile.dietaryPreference}' preference and '${profile.cuisinePreference}' cuisine style, focus on high protein sources (like chicken breast, lentils/daal, lean beef, or cottage cheese) paired with complex carbs (such as brown rice, roti, or quinoa). Tap on 'Diet Plan' in the bottom bar to generate your daily menu!"
            }
            msg.contains("hi") || msg.contains("hello") || msg.contains("hey") || msg.contains("help") -> {
                "Hello $userName! 👋 I am FitCoach, your local personal fitness AI advisor. Ask me anything about your current diet, workout routines, or progress. (Note: Put in your Gemini API key in Settings to unlock live conversational coaching!)"
            }
            else -> {
                "Hey there! That's a great fitness question. Sticking to a balanced lifestyle is 80% nutrition and 20% consistent movement. Try adjusting your workout modes between 'Gym' and 'Home', or tap on the 'Check-In' tab to record your body metrics and track your progress over time! You can also paste in your own Gemini API key in Settings for fully customized real-time answers."
            }
        }
    }
}
