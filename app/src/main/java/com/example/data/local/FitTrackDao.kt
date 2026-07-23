package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitTrackDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    fun getUserProfile(userId: Int = 1): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    suspend fun getUserProfileOnce(userId: Int = 1): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    // Daily Logs
    @Query("SELECT * FROM daily_logs WHERE dateString = :dateString AND userId = :userId LIMIT 1")
    fun getDailyLog(dateString: String, userId: Int = 1): Flow<DailyLog?>

    @Query("SELECT * FROM daily_logs WHERE dateString = :dateString AND userId = :userId LIMIT 1")
    suspend fun getDailyLogOnce(dateString: String, userId: Int = 1): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE userId = :userId ORDER BY dateString ASC")
    fun getAllDailyLogs(userId: Int = 1): Flow<List<DailyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(dailyLog: DailyLog)

    // Workout Logs
    @Query("SELECT * FROM workout_logs WHERE userId = :userId ORDER BY dateString DESC, id DESC")
    fun getAllWorkoutLogs(userId: Int = 1): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(workoutLog: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkoutLog(id: Long)

    // Meal Plan
    @Query("SELECT * FROM meal_plan_days WHERE dateString = :dateString AND userId = :userId LIMIT 1")
    fun getMealPlanDay(dateString: String, userId: Int = 1): Flow<MealPlanDay?>

    @Query("SELECT * FROM meal_plan_days WHERE dateString = :dateString AND userId = :userId LIMIT 1")
    suspend fun getMealPlanDayOnce(dateString: String, userId: Int = 1): MealPlanDay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlanDay(mealPlanDay: MealPlanDay)

    // Progress Photos
    @Query("SELECT * FROM progress_photos WHERE userId = :userId ORDER BY dateString DESC, id DESC")
    fun getAllProgressPhotos(userId: Int = 1): Flow<List<ProgressPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressPhoto(progressPhoto: ProgressPhoto)

    @Query("DELETE FROM progress_photos WHERE id = :id")
    suspend fun deleteProgressPhoto(id: Long)
}
