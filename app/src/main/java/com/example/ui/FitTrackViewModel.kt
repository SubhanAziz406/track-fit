package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiManager
import com.example.data.local.FitTrackDatabase
import com.example.data.model.*
import com.example.data.repository.FitTrackRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FitTrackViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FitTrackRepository
    
    init {
        val database = FitTrackDatabase.getDatabase(application)
        repository = FitTrackRepository(database.fitTrackDao())
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Date formatting helper
    val todayDateString: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

    // --- State Expositions ---
    val userProfile: StateFlow<UserProfile?> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allDailyLogs: StateFlow<List<DailyLog>> = repository.getAllDailyLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLog: StateFlow<DailyLog?> = repository.getDailyLog(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allWorkoutLogs: StateFlow<List<WorkoutLog>> = repository.getAllWorkoutLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMealPlan: StateFlow<MealPlanDay?> = repository.getMealPlanDay(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProgressPhotos: StateFlow<List<ProgressPhoto>> = repository.getAllProgressPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Local UI States ---
    var chatMessages = mutableStateListOf<Pair<String, Boolean>>() // Pair(Message, isUser)
        private set
    
    var isChatGenerating by mutableStateOf(false)
        private set

    var isDietGenerating by mutableStateOf(false)
        private set

    // --- Onboarding / Profile saving ---
    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile)
        }
    }

    // --- Daily Check-In logging ---
    fun saveCheckIn(
        weight: Float?,
        waist: Float?,
        chest: Float?,
        arms: Float?,
        thighs: Float?,
        mood: String,
        energy: Int,
        waterMl: Int,
        sleep: Float
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLog = repository.getDailyLogOnce(todayDateString)
            val newLog = DailyLog(
                dateString = todayDateString,
                weightKg = weight ?: currentLog?.weightKg,
                waistCm = waist ?: currentLog?.waistCm,
                chestCm = chest ?: currentLog?.chestCm,
                armsCm = arms ?: currentLog?.armsCm,
                thighsCm = thighs ?: currentLog?.thighsCm,
                mood = mood,
                energyLevel = energy,
                waterIntakeMl = waterMl,
                sleepHours = sleep
            )
            repository.saveDailyLog(newLog)
            
            // If weight is updated, optionally update user profile base weight too
            if (weight != null) {
                val profile = repository.getUserProfileOnce()
                if (profile != null) {
                    repository.saveUserProfile(profile.copy(weightKg = weight))
                }
            }
        }
    }

    fun addWater(ml: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = repository.getDailyLogOnce(todayDateString) ?: DailyLog(todayDateString)
            repository.saveDailyLog(log.copy(waterIntakeMl = log.waterIntakeMl + ml))
        }
    }

    // --- Diet Plan Operations ---
    fun generateOrFetchDietPlan(forceRegenerate: Boolean = false) {
        viewModelScope.launch {
            if (isDietGenerating) return@launch
            
            val currentPlan = repository.getMealPlanDayOnce(todayDateString)
            if (currentPlan != null && !forceRegenerate) {
                return@launch // Already exists
            }

            isDietGenerating = true
            val profile = repository.getUserProfileOnce() ?: UserProfile()
            
            try {
                val meals = AiManager.generateDietPlan(profile)
                val type = Types.newParameterizedType(List::class.java, Meal::class.java)
                val adapter = moshi.adapter<List<Meal>>(type)
                
                val mealPlanDay = MealPlanDay(
                    dateString = todayDateString,
                    mealsJson = adapter.toJson(meals),
                    generatedForProfileJson = moshi.adapter(UserProfile::class.java).toJson(profile)
                )
                repository.saveMealPlanDay(mealPlanDay)
            } catch (e: Exception) {
                Log.e("FitTrackViewModel", "Diet generation failed: ${e.message}")
            } finally {
                isDietGenerating = false
            }
        }
    }

    fun toggleMealEaten(mealType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val plan = repository.getMealPlanDayOnce(todayDateString) ?: return@launch
            val type = Types.newParameterizedType(List::class.java, Meal::class.java)
            val adapter = moshi.adapter<List<Meal>>(type)
            val currentMeals = adapter.fromJson(plan.mealsJson)?.toMutableList() ?: return@launch
            
            val index = currentMeals.indexOfFirst { it.mealType == mealType }
            if (index != -1) {
                val meal = currentMeals[index]
                currentMeals[index] = meal.copy(isEaten = !meal.isEaten)
                
                repository.saveMealPlanDay(plan.copy(mealsJson = adapter.toJson(currentMeals)))
            }
        }
    }

    fun regenerateSingleMeal(mealType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val plan = repository.getMealPlanDayOnce(todayDateString) ?: return@launch
            val type = Types.newParameterizedType(List::class.java, Meal::class.java)
            val adapter = moshi.adapter<List<Meal>>(type)
            val currentMeals = adapter.fromJson(plan.mealsJson)?.toMutableList() ?: return@launch
            
            val index = currentMeals.indexOfFirst { it.mealType == mealType }
            if (index != -1) {
                // Generate a replacement meal of the same type based on profile cuisine
                val profile = repository.getUserProfileOnce() ?: UserProfile()
                val targetCalories = when (profile.fitnessGoal) {
                    "Lose Weight" -> 1600
                    "Build Muscle" -> 2500
                    else -> 2000
                }
                val mealCal = when (mealType) {
                    "Breakfast" -> (targetCalories * 0.25).toInt()
                    "Lunch" -> (targetCalories * 0.35).toInt()
                    "Dinner" -> (targetCalories * 0.28).toInt()
                    else -> (targetCalories * 0.12).toInt()
                }

                // Generates customized replacements off-line (failsafe)
                val newMeal = when (profile.cuisinePreference) {
                    "South Asian", "Pakistani" -> when (mealType) {
                        "Breakfast" -> Meal(mealType, "Scrambled Paneer & Paratha (Low Oil)", "Spiced scrambled cottage cheese with tomatoes and onions, with a high-protein multigrain paratha.", mealCal, 24, 32, 10)
                        "Lunch" -> Meal(mealType, "Lean Beef Shami Kabab with Brown Rice", "Two pan-seared lentil-beef kababs, served with cumin boiled brown rice and fresh kachumber salad.", mealCal, 36, 48, 8)
                        "Dinner" -> Meal(mealType, "Daal Chawal with Steamed Vegetables", "Sautéed split yellow lentils served over wild red rice, accompanied by a side of roasted cauliflower.", mealCal, 18, 54, 4)
                        else -> Meal(mealType, "Roasted Chana & Almonds", "Handful of roasted yellow dry chickpeas seasoned with black pepper, paired with 8 raw almonds.", mealCal, 10, 18, 6)
                    }
                    "Mediterranean" -> when (mealType) {
                        "Breakfast" -> Meal(mealType, "Mediterranean Egg White Scramble", "Scrambled egg whites tossed with spinach, sun-dried tomatoes, and light goat cheese on gluten-free toast.", mealCal, 22, 24, 8)
                        "Lunch" -> Meal(mealType, "Grilled Seafood Quinoa Medley", "Seared prawns and calamari strips served over herbed quinoa, lemon zest, and sliced avocados.", mealCal, 38, 40, 10)
                        "Dinner" -> Meal(mealType, "Herb Baked Cod & Zucchini Noodles", "Pacific cod fillet seasoned with oregano, baked and served on a bed of warm garlic spiral zucchini noodles.", mealCal, 34, 15, 6)
                        else -> Meal(mealType, "Feta & Cherry Tomato Skewers", "Bite-sized skewers of low-fat Greek feta cheese and sweet grape tomatoes drizzled with balsamic glaze.", mealCal, 8, 8, 7)
                    }
                    else -> when (mealType) { // Western/Clean
                        "Breakfast" -> Meal(mealType, "Vanilla Almond Oatmeal", "Steel-cut oats cooked in unsweetened almond milk, mixed with one scoop of vanilla whey protein and chia seeds.", mealCal, 28, 30, 6)
                        "Lunch" -> Meal(mealType, "Honey Mustard Chicken Wrap", "Sliced grilled chicken breast, spinach, and low-calorie honey mustard spread rolled in a high-fiber tortilla.", mealCal, 34, 28, 8)
                        "Dinner" -> Meal(mealType, "Pan-Seared Turkey Medallions", "Lean ground turkey steaks pan-cooked with sage, served with baked broccoli spears and baked butternut squash.", mealCal, 38, 22, 7)
                        else -> Meal(mealType, "Cottage Cheese & Peaches", "Half cup of low-fat cottage cheese topped with fresh sliced peach segments and a dash of cinnamon.", mealCal, 14, 16, 2)
                    }
                }
                
                currentMeals[index] = newMeal
                repository.saveMealPlanDay(plan.copy(mealsJson = adapter.toJson(currentMeals)))
            }
        }
    }

    // --- Active Workout State ---
    var activeWorkoutRoutine by mutableStateOf<String?>(null) // e.g. "Gym Push Day", "Home Full Body"
    var activeWorkoutMode by mutableStateOf("Gym") // Gym, Home
    val activeExercises = mutableStateListOf<ExerciseSetLog>()
    var workoutDurationSeconds by mutableStateOf(0)
    var isWorkoutActive by mutableStateOf(false)
    private var workoutTimerJob: Job? = null

    // Rest countdown state
    var restTimeTotalSeconds by mutableStateOf(0)
    var restTimeRemainingSeconds by mutableStateOf(0)
    var isRestTimerRunning by mutableStateOf(false)
    private var restTimerJob: Job? = null

    fun startWorkout(title: String, mode: String, initialExercises: List<ExerciseSetLog>) {
        activeWorkoutRoutine = title
        activeWorkoutMode = mode
        activeExercises.clear()
        activeExercises.addAll(initialExercises)
        workoutDurationSeconds = 0
        isWorkoutActive = true
        
        // Start duration timer
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (isWorkoutActive) {
                delay(1000)
                workoutDurationSeconds++
            }
        }
    }

    fun triggerRestTimer(seconds: Int) {
        restTimeTotalSeconds = seconds
        restTimeRemainingSeconds = seconds
        isRestTimerRunning = true
        
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (restTimeRemainingSeconds > 0) {
                delay(1000)
                restTimeRemainingSeconds--
            }
            isRestTimerRunning = false
            playRestBeep()
        }
    }

    private fun playRestBeep() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
        } catch (e: Exception) {
            Log.e("FitTrackViewModel", "Beeper play failed: ${e.message}")
        }
    }

    fun updateWorkoutSet(exerciseIndex: Int, setIndex: Int, reps: Int, weight: Float, completed: Boolean) {
        if (exerciseIndex in activeExercises.indices) {
            val exercise = activeExercises[exerciseIndex]
            if (setIndex in exercise.sets.indices) {
                val setList = exercise.sets.toMutableList()
                setList[setIndex] = WorkoutSet(setNumber = setIndex + 1, reps = reps, weightKg = weight, isCompleted = completed)
                activeExercises[exerciseIndex] = exercise.copy(sets = setList)
            }
        }
    }

    fun finishWorkout() {
        if (!isWorkoutActive) return
        isWorkoutActive = false
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        isRestTimerRunning = false

        viewModelScope.launch(Dispatchers.IO) {
            val title = activeWorkoutRoutine ?: "Completed Session"
            val durationMin = (workoutDurationSeconds / 60).coerceAtLeast(1)
            
            // Auto-calculate estimated calories:
            // Gym mode ~ 6 kcal/min, Home mode ~ 8 kcal/min
            val rate = if (activeWorkoutMode == "Gym") 6 else 8
            val calories = durationMin * rate
            
            val type = Types.newParameterizedType(List::class.java, ExerciseSetLog::class.java)
            val adapter = moshi.adapter<List<ExerciseSetLog>>(type)
            val loggedExercises = activeExercises.toList()
            
            val log = WorkoutLog(
                dateString = todayDateString,
                title = title,
                durationMinutes = durationMin,
                caloriesBurned = calories,
                workoutMode = activeWorkoutMode,
                exercisesJson = adapter.toJson(loggedExercises)
            )
            repository.saveWorkoutLog(log)
            
            // Also log to DailyLog: Increment check-in water/mood metrics if present
            val todayLogVal = repository.getDailyLogOnce(todayDateString) ?: DailyLog(todayDateString)
            // Just update energyLevel or save a new log to prompt completeness
            repository.saveDailyLog(todayLogVal.copy(energyLevel = (todayLogVal.energyLevel + 1).coerceAtMost(5)))
            
            // Reset active state
            activeWorkoutRoutine = null
            activeExercises.clear()
        }
    }

    fun cancelActiveWorkout() {
        isWorkoutActive = false
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        isRestTimerRunning = false
        activeWorkoutRoutine = null
        activeExercises.clear()
    }

    // --- Progress Photo Operations ---
    fun addProgressPhoto(base64: String, label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val photo = ProgressPhoto(
                dateString = todayDateString,
                imageBase64 = base64,
                label = label
            )
            repository.saveProgressPhoto(photo)
        }
    }

    fun deleteProgressPhoto(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProgressPhoto(id)
        }
    }

    fun deleteWorkoutLog(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorkoutLog(id)
        }
    }

    // --- Chatbot Operations ---
    fun sendChatMessage(message: String) {
        if (message.trim().isEmpty() || isChatGenerating) return
        
        chatMessages.add(Pair(message, true))
        isChatGenerating = true
        
        viewModelScope.launch {
            val profile = repository.getUserProfileOnce() ?: UserProfile()
            val logs = allDailyLogs.value.takeLast(7)
            val workouts = allWorkoutLogs.value.takeLast(7)
            
            val reply = AiManager.getChatbotResponse(message, profile, logs, workouts)
            chatMessages.add(Pair(reply, false))
            isChatGenerating = false
        }
    }

    fun clearChat() {
        chatMessages.clear()
        chatMessages.add(Pair("Hello! I'm FitCoach, your personal AI trainer and nutritionist. Ready to crush your goals? Put in your API Key in Settings to enable real-time smart suggestions!", false))
    }

    init {
        clearChat()
    }

    // --- Data Export & Import (Production Feature!) ---
    data class FitTrackBackup(
        val profile: UserProfile?,
        val logs: List<DailyLog>,
        val workouts: List<WorkoutLog>,
        val mealPlans: List<MealPlanDay>,
        val photos: List<ProgressPhoto>
    )

    fun exportBackupString(onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = repository.getUserProfileOnce()
                val logs = repository.getAllDailyLogs().first()
                val workouts = repository.getAllWorkoutLogs().first()
                // Fetch progress photos and meal plans directly via Room flows (by calling first())
                val photos = repository.getAllProgressPhotos().first()
                
                // Let's retrieve today's meal plan list or similar
                val todayPlan = repository.getMealPlanDayOnce(todayDateString)
                val mealPlans = if (todayPlan != null) listOf(todayPlan) else emptyList()

                val backupObj = FitTrackBackup(profile, logs, workouts, mealPlans, photos)
                val adapter = moshi.adapter(FitTrackBackup::class.java)
                val json = adapter.toJson(backupObj)
                onComplete(json)
            } catch (e: Exception) {
                Log.e("FitTrackViewModel", "Export failed: ${e.message}")
                onComplete("")
            }
        }
    }

    fun importBackupString(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val adapter = moshi.adapter(FitTrackBackup::class.java)
                val backup = adapter.fromJson(json) ?: throw IllegalArgumentException("Invalid backup JSON")
                
                backup.profile?.let { repository.saveUserProfile(it) }
                backup.logs.forEach { repository.saveDailyLog(it) }
                backup.workouts.forEach { repository.saveWorkoutLog(it) }
                backup.mealPlans.forEach { repository.saveMealPlanDay(it) }
                backup.photos.forEach { repository.saveProgressPhoto(it) }
                
                onComplete(true)
            } catch (e: Exception) {
                Log.e("FitTrackViewModel", "Import failed: ${e.message}")
                onComplete(false)
            }
        }
    }
}
