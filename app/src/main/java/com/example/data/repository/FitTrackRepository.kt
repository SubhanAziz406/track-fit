package com.example.data.repository

import com.example.data.local.FitTrackDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class FitTrackRepository(private val dao: FitTrackDao) {

    // User Profile
    fun getUserProfile(userId: Int = 1): Flow<UserProfile?> = dao.getUserProfile(userId)
    
    suspend fun getUserProfileOnce(userId: Int = 1): UserProfile? = dao.getUserProfileOnce(userId)
    
    suspend fun saveUserProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    // Daily Logs
    fun getDailyLog(dateString: String, userId: Int = 1): Flow<DailyLog?> = dao.getDailyLog(dateString, userId)
    
    suspend fun getDailyLogOnce(dateString: String, userId: Int = 1): DailyLog? = dao.getDailyLogOnce(dateString, userId)
    
    fun getAllDailyLogs(userId: Int = 1): Flow<List<DailyLog>> = dao.getAllDailyLogs(userId)
    
    suspend fun saveDailyLog(log: DailyLog) {
        dao.insertDailyLog(log)
    }

    // Workout Logs
    fun getAllWorkoutLogs(userId: Int = 1): Flow<List<WorkoutLog>> = dao.getAllWorkoutLogs(userId)
    
    suspend fun saveWorkoutLog(log: WorkoutLog) {
        dao.insertWorkoutLog(log)
    }
    
    suspend fun deleteWorkoutLog(id: Long) {
        dao.deleteWorkoutLog(id)
    }

    // Meal Plans
    fun getMealPlanDay(dateString: String, userId: Int = 1): Flow<MealPlanDay?> = dao.getMealPlanDay(dateString, userId)
    
    suspend fun getMealPlanDayOnce(dateString: String, userId: Int = 1): MealPlanDay? = dao.getMealPlanDayOnce(dateString, userId)
    
    suspend fun saveMealPlanDay(mealPlanDay: MealPlanDay) {
        dao.insertMealPlanDay(mealPlanDay)
    }

    // Progress Photos
    fun getAllProgressPhotos(userId: Int = 1): Flow<List<ProgressPhoto>> = dao.getAllProgressPhotos(userId)
    
    suspend fun saveProgressPhoto(photo: ProgressPhoto) {
        dao.insertProgressPhoto(photo)
    }
    
    suspend fun deleteProgressPhoto(id: Long) {
        dao.deleteProgressPhoto(id)
    }
}
