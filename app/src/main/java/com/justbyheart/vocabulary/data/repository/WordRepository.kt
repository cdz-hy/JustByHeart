package com.justbyheart.vocabulary.data.repository

import androidx.lifecycle.LiveData
import com.justbyheart.vocabulary.data.dao.*
import com.justbyheart.vocabulary.data.entity.*
import java.util.Date

/**
 * 单词数据仓库类
 * 
 * 作为数据层的统一访问接口，封装了所有数据库操作。
 * 遵循Repository模式，为上层提供清晰的数据访问API，
 * 隐藏了具体的数据源实现细节。
 * 
 * @param wordDao 单词数据访问对象
 * @param studyRecordDao 学习记录数据访问对象
 * @param favoriteWordDao 收藏单词数据访问对象
 * @param dailyGoalDao 每日目标数据访问对象
 */
class WordRepository(
    private val wordDao: WordDao,
    private val studyRecordDao: StudyRecordDao,
    private val favoriteWordDao: FavoriteWordDao,
    private val dailyGoalDao: DailyGoalDao
) {
    
    // ==================== 单词相关操作 ====================
    
    /**
     * 获取所有单词
     * @return LiveData包装的单词列表
     */
    fun getAllWords(): LiveData<List<Word>> = wordDao.getAllWords()
    
    /**
     * 获取所有单词 (List)
     * @return 单词列表
     */
    suspend fun getAllWordsList(): List<Word> = wordDao.getAllWordsList()
    
    /**
     * 根据ID获取单词
     * @param id 单词ID
     * @return 单词对象或null
     */
    suspend fun getWordById(id: Long): Word? = wordDao.getWordById(id)
    
    /**
     * 随机获取指定数量的单词
     * @param count 单词数量
     * @return 单词列表
     */
    suspend fun getRandomWords(count: Int): List<Word> = wordDao.getRandomWords(count)
    
    suspend fun getUncompletedWords(count: Int): List<Word> = wordDao.getUncompletedWords(count)

    /**
     * 批量插入单词
     * @param words 单词列表
     */
    suspend fun insertWords(words: List<Word>) = wordDao.insertWords(words)
    
    // ==================== 学习记录相关操作 ====================
    
    /**
     * 根据日期获取学习记录
     * @param date 学习日期
     * @return LiveData包装的学习记录列表
     */
    fun getStudyRecordsByDate(date: Date): LiveData<List<StudyRecord>> = 
        studyRecordDao.getStudyRecordsByDate(date)
    
    /**
     * 插入学习记录
     * @param studyRecord 学习记录对象
     */
    suspend fun insertStudyRecord(studyRecord: StudyRecord) = 
        studyRecordDao.insertStudyRecord(studyRecord)
    
    /**
     * 更新学习记录
     * @param studyRecord 学习记录对象
     */
    suspend fun updateStudyRecord(studyRecord: StudyRecord) = 
        studyRecordDao.updateStudyRecord(studyRecord)
    
    /**
     * 根据单词ID和日期获取学习记录
     * @param wordId 单词ID
     * @param studyDate 学习日期
     * @return 学习记录对象或null
     */
    suspend fun getCompletedWordIdsForDate(date: Date): List<Long> = studyRecordDao.getCompletedWordIdsForDate(date)

    suspend fun getWordsByIds(ids: List<Long>): List<Word> = wordDao.getWordsByIds(ids)

    suspend fun getStudyRecordByWordIdAndDate(wordId: Long, date: Date): StudyRecord? = 
        studyRecordDao.getStudyRecordByWordIdAndDate(wordId, date)
    
    /**
     * 获取所有学习日期
     * @return LiveData包装的日期列表
     */
    fun getAllStudyDates(): LiveData<List<Date>> = studyRecordDao.getAllStudyDates()
    
    // ==================== 收藏相关操作 ====================
    
    /**
     * 获取所有收藏的单词
     * @return LiveData包装的收藏单词列表
     */
    fun getFavoriteWords(): LiveData<List<Word>> = favoriteWordDao.getFavoriteWords()
    
    /**
     * 添加单词到收藏夹
     * @param wordId 单词ID
     */
    suspend fun addToFavorites(wordId: Long) {
        favoriteWordDao.insertFavorite(FavoriteWord(wordId = wordId))
    }
    
    /**
     * 从收藏夹移除单词
     * @param wordId 单词ID
     */
    suspend fun removeFromFavorites(wordId: Long) {
        favoriteWordDao.deleteFavoriteByWordId(wordId)
    }
    
    /**
     * 检查单词是否已收藏
     * @param wordId 单词ID
     * @return true表示已收藏，false表示未收藏
     */
    suspend fun isFavorite(wordId: Long): Boolean = 
        favoriteWordDao.isFavorite(wordId) > 0
    
    // ==================== 每日目标相关操作 ====================
    
    /**
     * 根据日期获取每日目标
     * @param date 目标日期
     * @return 每日目标对象或null
     */
    suspend fun getDailyGoalByDate(date: Date): DailyGoal? = 
        dailyGoalDao.getDailyGoalByDate(date)
    
    /**
     * 插入每日目标
     * @param dailyGoal 每日目标对象
     */
    suspend fun insertDailyGoal(dailyGoal: DailyGoal) = 
        dailyGoalDao.insertDailyGoal(dailyGoal)
    
    /**
     * 更新每日目标
     * @param dailyGoal 每日目标对象
     */
    suspend fun updateDailyGoal(dailyGoal: DailyGoal) = 
        dailyGoalDao.updateDailyGoal(dailyGoal)
    
    /**
     * 获取最近的每日目标记录
     * @return LiveData包装的每日目标列表
     */
    fun getRecentDailyGoals(): LiveData<List<DailyGoal>> = 
        dailyGoalDao.getRecentDailyGoals()

    suspend fun getCompletedWordsCountForSpecificWords(wordIds: List<Long>, date: Date): Int = 
        studyRecordDao.getCompletedWordsCountForSpecificWords(wordIds, date)

    suspend fun getAdditionalUncompletedWords(count: Int, excludeIds: List<Long>): List<Word> = wordDao.getAdditionalUncompletedWords(count, excludeIds)

    suspend fun getMemorizedWordsCount(): Int = studyRecordDao.getMemorizedWordsCount()

    suspend fun getTotalWordsCount(): Int = wordDao.getWordCount()
}