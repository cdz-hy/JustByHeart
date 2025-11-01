package com.justbyheart.vocabulary.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.justbyheart.vocabulary.data.entity.StudyRecord
import com.justbyheart.vocabulary.data.entity.Word
import java.util.Date

/**
 * 学习记录数据访问对象接口
 * 
 * 定义了对学习记录表的所有数据库操作方法，
 * 包括增删改查等操作。
 */
@Dao
interface StudyRecordDao {
    /**
     * 根据日期查询学习记录
     * @param date 学习日期
     * @return 指定日期的学习记录列表
     */
    @Query("SELECT * FROM study_records WHERE studyDate = :date")
    fun getStudyRecordsByDate(date: Date): LiveData<List<StudyRecord>>

    /**
     * 根据单词ID查询学习记录
     * @param wordId 单词ID
     * @return 指定单词的所有学习记录，按日期倒序排列
     */
    @Query("SELECT * FROM study_records WHERE wordId = :wordId ORDER BY studyDate DESC")
    suspend fun getStudyRecordsByWordId(wordId: Long): List<StudyRecord>

    /**
     * 获取所有不同的学习日期
     * @return 所有学习日期列表，按日期倒序排列
     */
    @Query("SELECT DISTINCT studyDate FROM study_records ORDER BY studyDate DESC")
    fun getAllStudyDates(): LiveData<List<Date>>

    /**
     * 查询指定日期范围内的学习记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 指定日期范围内的学习记录列表
     */
    @Query("SELECT * FROM study_records WHERE studyDate BETWEEN :startDate AND :endDate")
    suspend fun getStudyRecordsBetweenDates(startDate: Date, endDate: Date): List<StudyRecord>

    /**
     * 插入学习记录
     * @param studyRecord 学习记录对象
     * @return 插入记录的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyRecord(studyRecord: StudyRecord): Long

    /**
     * 更新学习记录
     * @param studyRecord 学习记录对象
     */
    @Update
    suspend fun updateStudyRecord(studyRecord: StudyRecord)

    /**
     * 删除学习记录
     * @param studyRecord 学习记录对象
     */
    @Delete
    suspend fun deleteStudyRecord(studyRecord: StudyRecord)

    /**
     * 查询指定日期已完成的单词数量
     * @param date 学习日期
     * @return 指定日期已完成的单词数量
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE studyDate = :date AND isCompleted = 1")
    suspend fun getCompletedWordsCountByDate(date: Date): Int

    /**
     * 查询已掌握的单词数量
     * @return 已掌握的不重复单词数量
     */
    @Query("SELECT COUNT(DISTINCT wordId) FROM study_records WHERE isCompleted = 1")
    suspend fun getMemorizedWordsCount(): Int
    
    @Query("SELECT COUNT(DISTINCT wordId) FROM study_records WHERE isCompleted = 1 AND wordId IN (:wordIds)")
    suspend fun getMemorizedWordsCountByIds(wordIds: List<Long>): Int
    
    @Query("SELECT wordId FROM study_records WHERE DATE(studyDate/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch') AND isCompleted = 1")
    suspend fun getCompletedWordIdsForDate(date: Date): List<Long>

    @Query("SELECT COUNT(DISTINCT wordId) FROM study_records WHERE wordId IN (:wordIds) AND studyDate = :date AND isCompleted = 1")
    suspend fun getCompletedWordsCountForSpecificWords(wordIds: List<Long>, date: Date): Int

    @Query("SELECT w.* FROM words w INNER JOIN study_records s ON w.id = s.wordId WHERE s.studyDate = :date AND s.isCompleted = 1 ORDER BY w.id ASC")
    suspend fun getCompletedWordsForDate(date: Date): List<Word>

    /**
     * 根据单词ID列表获取已完成的单词
     * @param wordIds 单词ID列表
     * @return 已完成的单词列表
     */
    @Query("SELECT w.* FROM words w INNER JOIN study_records s ON w.id = s.wordId WHERE w.id IN (:wordIds) AND s.isCompleted = 1")
    suspend fun getCompletedWordsByIds(wordIds: List<Long>): List<Word>

    /**
     * 根据单词ID和学习日期获取学习记录
     * @param wordId 单词ID
     * @param studyDate 学习日期
     * @return 对应的学习记录，如果不存在则返回null
     */
    @Query("SELECT * FROM study_records WHERE wordId = :wordId AND studyDate = :studyDate")
    suspend fun getStudyRecordByWordIdAndDate(wordId: Long, studyDate: Date): StudyRecord?
    
    /**
     * 获取指定日期标记为已背的单词
     * @param date 学习日期
     * @return 指定日期标记为已背的单词列表
     */
    @Query("SELECT w.* FROM words w INNER JOIN study_records s ON w.id = s.wordId WHERE s.studyDate = :date AND s.isCompleted = 1 AND s.correctCount = 0 AND s.wrongCount = 0")
    suspend fun getMemorizedWordsByDate(date: Date): List<Word>
}