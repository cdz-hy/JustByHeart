package com.justbyheart.vocabulary.data.repository

import androidx.lifecycle.LiveData
import com.justbyheart.vocabulary.data.dao.DailyGoalDao
import com.justbyheart.vocabulary.data.dao.FavoriteWordDao
import com.justbyheart.vocabulary.data.dao.StudyRecordDao
import com.justbyheart.vocabulary.data.dao.WordDao
import com.justbyheart.vocabulary.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.*

class WordRepository(
    private val wordDao: WordDao,
    private val studyRecordDao: StudyRecordDao,
    private val favoriteWordDao: FavoriteWordDao,
    private val dailyGoalDao: DailyGoalDao
) {
    // 单词相关操作
    fun getAllWords(): LiveData<List<Word>> = wordDao.getAllWords()
    
    suspend fun getWordById(id: Long): Word? = wordDao.getWordById(id)
    
    suspend fun getRandomWords(count: Int): List<Word> = wordDao.getRandomWords(count)
    
    suspend fun getRandomWordsByCategory(category: String, count: Int): List<Word> = 
        wordDao.getRandomWordsByCategory(category, count)
    
    suspend fun getWordsByWordBank(wordBank: String): List<Word> = wordDao.getWordsByWordBank(wordBank)
    
    suspend fun getRandomWordsByWordBank(wordBank: String, count: Int): List<Word> = 
        wordDao.getRandomWordsByWordBank(wordBank, count)
    
    suspend fun getWordByEnglishAndWordBank(english: String, wordBank: String): Word? = 
        wordDao.getWordByEnglishAndWordBank(english, wordBank)
    
    suspend fun getWordCountByWordBank(wordBank: String): Int = wordDao.getWordCountByWordBank(wordBank)
    
    suspend fun insertWord(word: Word): Long = wordDao.insertWord(word)
    
    suspend fun insertWords(words: List<Word>) = wordDao.insertWords(words)
    
    suspend fun updateWord(word: Word) = wordDao.updateWord(word)
    
    suspend fun deleteWord(word: Word) = wordDao.deleteWord(word)
    
    suspend fun deleteWordsByWordBank(wordBank: String): Int = wordDao.deleteWordsByWordBank(wordBank)
    
    suspend fun getUncompletedWords(count: Int): List<Word> = wordDao.getUncompletedWords(count)
    
    suspend fun getUncompletedWordsByWordBank(wordBank: String, count: Int): List<Word> {
        return wordDao.getUncompletedWordsByWordBank(wordBank, count)
    }
    
    suspend fun getAdditionalUncompletedWordsByWordBank(wordBank: String, count: Int, excludeIds: List<Long>): List<Word> {
        return wordDao.getAdditionalUncompletedWordsByWordBank(wordBank, count, excludeIds)
    }
    
    // 添加一个新的方法，按词库获取未完成的单词并按ID排序（保证获取最早添加的单词）
    suspend fun getUncompletedWordsByWordBankOrdered(wordBank: String, count: Int): List<Word> {
        // 直接在数据库查询中按词库过滤，提高效率
        val allUncompletedWords = wordDao.getUncompletedWordsByWordBank(wordBank, count * 2) // 获取更多单词以防过滤后不足
        return allUncompletedWords
            .sortedBy { it.id } // 按ID排序，确保获取最早添加的单词
            .take(count)
    }
    
    suspend fun getWordsByIds(ids: List<Long>): List<Word> = wordDao.getWordsByIds(ids)
    
    suspend fun getAdditionalUncompletedWords(count: Int, excludeIds: List<Long>): List<Word> = 
        wordDao.getAdditionalUncompletedWords(count, excludeIds)
    
    suspend fun getCompletedWords(): List<Word> = wordDao.getCompletedWords()
    
    suspend fun getWordCount(): Int = wordDao.getWordCount()
    
    suspend fun deleteAllWords(): Int = wordDao.deleteAllWords()
    
    /**
     * 在指定词库中搜索单词
     * @param query 搜索关键词
     * @param wordBank 词库名称
     * @return 匹配的单词列表
     */
    suspend fun searchWordsInWordBank(query: String, wordBank: String): List<Word> {
        return wordDao.searchWordsInWordBank(query, wordBank)
    }
    
    // 学习记录相关操作
    suspend fun insertStudyRecord(studyRecord: StudyRecord) = studyRecordDao.insertStudyRecord(studyRecord)
    
    suspend fun updateStudyRecord(studyRecord: StudyRecord) = studyRecordDao.updateStudyRecord(studyRecord)
    
    suspend fun getStudyRecordByWordIdAndDate(wordId: Long, date: Date): StudyRecord? = 
        studyRecordDao.getStudyRecordByWordIdAndDate(wordId, date)
    
    fun getStudyRecordsByDate(date: Date): LiveData<List<StudyRecord>> = 
        studyRecordDao.getStudyRecordsByDate(date)
    
    suspend fun getStudyRecordsByWordId(wordId: Long): List<StudyRecord> = studyRecordDao.getStudyRecordsByWordId(wordId)
    
    suspend fun getStudyRecordsBetweenDates(startDate: Date, endDate: Date): List<StudyRecord> = 
        studyRecordDao.getStudyRecordsBetweenDates(startDate, endDate)
    
    suspend fun deleteStudyRecord(studyRecord: StudyRecord) = studyRecordDao.deleteStudyRecord(studyRecord)
    
    suspend fun getCompletedWordsCountByDate(date: Date): Int = studyRecordDao.getCompletedWordsCountByDate(date)
    
    suspend fun getMemorizedWordsCount(): Int = studyRecordDao.getMemorizedWordsCount()
    
    suspend fun getMemorizedWordsCountByWordBank(wordBank: String): Int {
        val wordsInBank = getWordsByWordBank(wordBank)
        val wordIds = wordsInBank.map { it.id }
        if (wordIds.isEmpty()) return 0
        
        return studyRecordDao.getMemorizedWordsCountByIds(wordIds)
    }
    
    suspend fun getCompletedWordIdsForDate(date: Date): List<Long> = studyRecordDao.getCompletedWordIdsForDate(date)
    
    suspend fun getCompletedWordsCountForSpecificWords(wordIds: List<Long>, date: Date): Int = 
        studyRecordDao.getCompletedWordsCountForSpecificWords(wordIds, date)
    
    suspend fun getCompletedWordsForDate(date: Date): List<Word> = studyRecordDao.getCompletedWordsForDate(date)
    
    /**
     * 获取指定词库中已完成的单词
     * @param wordBank 词库名称
     * @return 指定词库中已完成的单词列表
     */
    suspend fun getCompletedWordsByWordBank(wordBank: String): List<Word> {
        val words = getWordsByWordBank(wordBank)
        val wordIds = words.map { it.id }
        if (wordIds.isEmpty()) return emptyList()
        
        return studyRecordDao.getCompletedWordsByIds(wordIds)
    }
    
    /**
     * 迁移学习记录到新的词库
     * @param fromWordBank 原词库名称
     * @param toWordBank 目标词库名称
     */
    suspend fun migrateStudyRecords(fromWordBank: String, toWordBank: String) {
        // 获取原词库中已完成的单词英文名称
        val completedWordsInFromBank = getCompletedWordsByWordBank(fromWordBank)
        val completedEnglishWords = completedWordsInFromBank.map { it.english }
        
        if (completedEnglishWords.isEmpty()) return
        
        // 在目标词库中查找对应的单词
        for (englishWord in completedEnglishWords) {
            val targetWord = getWordByEnglishAndWordBank(englishWord, toWordBank)
            targetWord?.let { word ->
                // 检查是否已经有对应的学习记录
                val today = getTodayZeroed()
                val existingRecord = getStudyRecordByWordIdAndDate(word.id, today)
                
                // 如果没有记录，则创建新的学习记录
                if (existingRecord == null) {
                    val newRecord = StudyRecord(
                        wordId = word.id,
                        studyDate = today,
                        isCompleted = true
                    )
                    insertStudyRecord(newRecord)
                }
            }
        }
    }
    
    /**
     * 迁移收藏记录到新的词库
     * @param fromWordBank 原词库名称
     * @param toWordBank 目标词库名称
     */
    suspend fun migrateFavoriteWords(fromWordBank: String, toWordBank: String) {
        // 获取原词库中收藏的单词英文名称
        val favoriteWordsInFromBank = getFavoriteWordsByWordBank(fromWordBank)
        val favoriteEnglishWords = favoriteWordsInFromBank.map { it.english }
        
        if (favoriteEnglishWords.isEmpty()) return
        
        // 在目标词库中查找对应的单词
        for (englishWord in favoriteEnglishWords) {
            val targetWord = getWordByEnglishAndWordBank(englishWord, toWordBank)
            targetWord?.let { word ->
                // 检查是否已经有对应的收藏记录
                val existingFavorite = getFavoriteWordByWordId(word.id)
                
                // 如果没有记录，则创建新的收藏记录
                if (existingFavorite == null) {
                    val newFavorite = FavoriteWord(
                        wordId = word.id,
                        addedDate = Date()
                    )
                    insertFavoriteWord(newFavorite)
                }
            }
        }
    }
    
    // 收藏单词相关操作
    suspend fun insertFavoriteWord(favoriteWord: FavoriteWord): Long = favoriteWordDao.insertFavorite(favoriteWord)
    
    suspend fun deleteFavoriteWordByWordId(wordId: Long) = favoriteWordDao.deleteFavoriteByWordId(wordId)
    
    suspend fun getFavoriteWordByWordId(wordId: Long): FavoriteWord? = favoriteWordDao.getFavoriteByWordId(wordId)
    
    fun getFavoriteWords(): LiveData<List<Word>> = favoriteWordDao.getFavoriteWords()
    
    suspend fun isWordFavorite(wordId: Long): Boolean = favoriteWordDao.isFavorite(wordId) > 0
    
    /**
     * 获取指定词库中的收藏单词
     * @param wordBank 词库名称
     * @return 指定词库中的收藏单词列表
     */
    suspend fun getFavoriteWordsByWordBank(wordBank: String): List<Word> {
        val allFavoriteWords = getFavoriteWordsList()
        return allFavoriteWords.filter { it.wordBank == wordBank }
    }
    
    /**
     * 获取所有收藏单词（非LiveData版本）
     * @return 所有收藏单词列表
     */
    suspend fun getFavoriteWordsList(): List<Word> {
        // 这里需要实现获取所有收藏单词的逻辑
        // 由于FavoriteWordDao.getFavoriteWords()返回的是LiveData，我们需要另外的方法
        return favoriteWordDao.getAllFavoriteWordsWithDetails()
    }
    
    // 每日目标相关操作
    suspend fun insertDailyGoal(dailyGoal: DailyGoal) = dailyGoalDao.insertDailyGoal(dailyGoal)
    
    suspend fun updateDailyGoal(dailyGoal: DailyGoal) = dailyGoalDao.updateDailyGoal(dailyGoal)
    
    suspend fun deleteDailyGoal(dailyGoal: DailyGoal) = dailyGoalDao.deleteDailyGoal(dailyGoal)
    
    suspend fun getDailyGoalByDate(date: Date): DailyGoal? = dailyGoalDao.getDailyGoalByDate(date)
    
    fun getRecentDailyGoals(): LiveData<List<DailyGoal>> = dailyGoalDao.getRecentDailyGoals()
    
    suspend fun createOrUpdateDailyGoal(targetCount: Int) {
        val today = getTodayZeroed()
        val existingGoal = getDailyGoalByDate(today)
        
        if (existingGoal != null) {
            // 更新现有目标
            val updatedGoal = existingGoal.copy(targetWordCount = targetCount)
            updateDailyGoal(updatedGoal)
        } else {
            // 创建新目标
            val newGoal = DailyGoal(
                date = today,
                targetWordCount = targetCount,
                completedWordCount = 0
            )
            insertDailyGoal(newGoal)
        }
    }
    
    suspend fun updateDailyGoalCompletedCount(date: Date, completedCount: Int) {
        val existingGoal = getDailyGoalByDate(date)
        if (existingGoal != null) {
            val updatedGoal = existingGoal.copy(completedWordCount = completedCount)
            updateDailyGoal(updatedGoal)
        }
    }
    
    suspend fun getOrCreateTodayGoal(targetCount: Int): DailyGoal {
        val today = getTodayZeroed()
        val existingGoal = getDailyGoalByDate(today)
        
        return if (existingGoal != null) {
            existingGoal
        } else {
            val newGoal = DailyGoal(
                date = today,
                targetWordCount = targetCount,
                completedWordCount = 0
            )
            insertDailyGoal(newGoal)
            newGoal
        }
    }
    
    // 辅助方法
    private fun getTodayZeroed(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}