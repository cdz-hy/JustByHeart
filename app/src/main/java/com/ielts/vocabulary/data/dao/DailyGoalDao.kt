package com.ielts.vocabulary.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ielts.vocabulary.data.entity.DailyGoal
import java.util.Date

@Dao
interface DailyGoalDao {
    @Query("SELECT * FROM daily_goals WHERE date = :date")
    suspend fun getDailyGoalByDate(date: Date): DailyGoal?

    @Query("SELECT * FROM daily_goals ORDER BY date DESC LIMIT 7")
    fun getRecentDailyGoals(): LiveData<List<DailyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyGoal(dailyGoal: DailyGoal): Long

    @Update
    suspend fun updateDailyGoal(dailyGoal: DailyGoal)

    @Delete
    suspend fun deleteDailyGoal(dailyGoal: DailyGoal)
}