package com.ielts.vocabulary.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ielts.vocabulary.data.entity.StudyRecord
import java.util.Date

@Dao
interface StudyRecordDao {
    @Query("SELECT * FROM study_records WHERE studyDate = :date")
    fun getStudyRecordsByDate(date: Date): LiveData<List<StudyRecord>>

    @Query("SELECT * FROM study_records WHERE wordId = :wordId ORDER BY studyDate DESC")
    suspend fun getStudyRecordsByWordId(wordId: Long): List<StudyRecord>

    @Query("SELECT DISTINCT studyDate FROM study_records ORDER BY studyDate DESC")
    fun getAllStudyDates(): LiveData<List<Date>>

    @Query("SELECT * FROM study_records WHERE studyDate BETWEEN :startDate AND :endDate")
    suspend fun getStudyRecordsBetweenDates(startDate: Date, endDate: Date): List<StudyRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyRecord(studyRecord: StudyRecord): Long

    @Update
    suspend fun updateStudyRecord(studyRecord: StudyRecord)

    @Delete
    suspend fun deleteStudyRecord(studyRecord: StudyRecord)

    @Query("SELECT COUNT(*) FROM study_records WHERE studyDate = :date AND isCompleted = 1")
    suspend fun getCompletedWordsCountByDate(date: Date): Int
}