package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 25,
    val gender: String = "Male",
    val heightCm: Float = 175f,
    val weightKg: Float = 70f,
    val activityLevel: String = "Moderate", // Sedentary, Light, Moderate, Active, Very Active
    val fitnessGoal: String = "Lose Weight", // Lose Weight, Maintain, Build Muscle
    val dietaryPreference: String = "Non-Veg", // Veg, Non-Veg, Vegan, Halal
    val allergies: String = "",
    val cuisinePreference: String = "Mediterranean",
    val isOnboarded: Boolean = false,
    val aiApiKey: String = "" // Custom API Key (optional)
)

@Entity(tableName = "daily_logs", primaryKeys = ["dateString", "userId"])
data class DailyLog(
    val dateString: String, // "YYYY-MM-DD"
    val userId: Int = 1,    // Owner ID for multi-user scaling
    val weightKg: Float? = null,
    val waistCm: Float? = null,
    val chestCm: Float? = null,
    val armsCm: Float? = null,
    val thighsCm: Float? = null,
    val mood: String = "Good", // Great, Good, Neutral, Tired, Stressed
    val energyLevel: Int = 3, // 1 to 5
    val waterIntakeMl: Int = 0,
    val sleepHours: Float = 7f
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val userId: Int = 1,    // Owner ID for multi-user scaling
    val title: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val workoutMode: String, // Gym, Home
    val exercisesJson: String // JSON of List<ExerciseSetLog>
)

@Entity(tableName = "meal_plan_days", primaryKeys = ["dateString", "userId"])
data class MealPlanDay(
    val dateString: String, // "YYYY-MM-DD"
    val userId: Int = 1,    // Owner ID for multi-user scaling
    val mealsJson: String, // JSON of List<Meal>
    val generatedForProfileJson: String // Snapshot of UserProfile used for generation
)

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val userId: Int = 1,    // Owner ID for multi-user scaling
    val imageBase64: String, // Base64 encoded image
    val label: String = "Progress Entry"
)

// Helper classes for serialization
data class ExerciseSetLog(
    val exerciseName: String,
    val category: String, // Push, Pull, Legs, Core, Cardio
    val sets: List<WorkoutSet>
)

data class WorkoutSet(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float,
    val isCompleted: Boolean = false
)

data class Meal(
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val name: String,
    val description: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val isEaten: Boolean = false
)
