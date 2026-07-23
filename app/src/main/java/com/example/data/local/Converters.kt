package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ExerciseSetLog
import com.example.data.model.Meal
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromExerciseSetLogList(value: List<ExerciseSetLog>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, ExerciseSetLog::class.java)
        val adapter = moshi.adapter<List<ExerciseSetLog>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toExerciseSetLogList(value: String?): List<ExerciseSetLog>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, ExerciseSetLog::class.java)
        val adapter = moshi.adapter<List<ExerciseSetLog>>(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromMealList(value: List<Meal>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, Meal::class.java)
        val adapter = moshi.adapter<List<Meal>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toMealList(value: String?): List<Meal>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, Meal::class.java)
        val adapter = moshi.adapter<List<Meal>>(type)
        return adapter.fromJson(value)
    }
}
