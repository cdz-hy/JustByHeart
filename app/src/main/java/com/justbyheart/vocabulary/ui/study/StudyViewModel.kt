package com.justbyheart.vocabulary.ui.study

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.VocabularyApplication
import com.justbyheart.vocabulary.data.entity.DailyGoal
import com.justbyheart.vocabulary.data.entity.StudyRecord
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.utils.WordDataLoader
import kotlinx.coroutines.launch
import java.util.*

/**
 * 学习页面ViewModel
 * 
 * 负责管理学习页面的业务逻辑，包括：
 * - 加载今日学习单词
 * - 跟踪当前学习位置
 * - 管理单词收藏状态
 * - 更新学习进度和完成状态
 * 
 * @param repository 数据仓库，用于数据访问
 */
class StudyViewModel(
    private val repository: WordRepository
) : ViewModel() {
    
    // 今日学习单词列表的私有可变LiveData
    private val _todayWords = MutableLiveData<List<Word>>()
    // 对外暴露的只读LiveData
    val todayWords: LiveData<List<Word>> = _todayWords
    
    // 当前学习位置的私有可变LiveData
    private val _currentPosition = MutableLiveData<Int>()
    // 对外暴露的只读LiveData
    val currentPosition: LiveData<Int> = _currentPosition
    
    // 加载状态的私有可变LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    // 对外暴露的只读LiveData
    val isLoading: LiveData<Boolean> = _isLoading
    
    val favoriteWords: LiveData<List<Word>> = repository.getFavoriteWords()
    
    // 翻转单词列表的私有可变LiveData
    private val _flippedWords = MutableLiveData<Set<Long>>(emptySet())
    // 对外暴露的只读LiveData
    val flippedWords: LiveData<Set<Long>> = _flippedWords

    private var todaysWordList: List<Word>? = null
    private var dateOfTodaysWordList: Date? = null
    
    // SharedPreferences常量
    private companion object {
        const val PREFS_NAME = "vocabulary_settings"
        const val KEY_DAILY_WORD_COUNT = "daily_word_count"   // 每日单词数键名
        const val KEY_CURRENT_WORD_BANK = "current_word_bank"
        const val DEFAULT_WORD_BANK = "六级核心"
        const val DEFAULT_DAILY_WORD_COUNT = 10               // 默认每日单词数
    }
    
    /**
     * 加载今日学习单词
     * 
     * 根据今日的学习目标数量，获取相应数量的单词，
     */
    fun loadTodayWords(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val today = getTodayZeroed()
            
            // 获取当前词库
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
            
            // 获取用户设置的每日单词数
            val userSetDailyWordCount = sharedPreferences.getInt(KEY_DAILY_WORD_COUNT, DEFAULT_DAILY_WORD_COUNT)

            var dailyGoal = repository.getDailyGoalByDate(today)
            var currentDailyWordIds: List<Long>

            // 如果今日目标不存在，或者每日单词ID列表为空，则生成新的今日单词列表并保存
            if (dailyGoal == null || dailyGoal.dailyWordIds.isEmpty()) {
                // 从当前词库中获取未完成的单词（按ID排序，获取最早添加的单词）
                val newWords = repository.getUncompletedWordsByWordBankOrdered(currentWordBank, dailyGoal?.targetWordCount ?: userSetDailyWordCount)
                currentDailyWordIds = newWords.map { it.id }
                
                val updatedGoal = (dailyGoal ?: DailyGoal(date = today)).copy(
                    dailyWordIds = currentDailyWordIds.joinToString(","),
                    targetWordCount = dailyGoal?.targetWordCount ?: userSetDailyWordCount,
                    flippedWordIds = dailyGoal?.flippedWordIds ?: ""
                )
                repository.insertDailyGoal(updatedGoal)
                dailyGoal = updatedGoal
            } else {
                // 如果今日目标存在且每日单词ID列表不为空，则使用存储的ID列表
                currentDailyWordIds = dailyGoal.dailyWordIds.split(",").map { it.toLong() }

                // 检查目标中的单词是否都属于当前词库
                val wordsInGoal = repository.getWordsByIds(currentDailyWordIds)
                val wordsNotInCurrentBank = wordsInGoal.filter { it.wordBank != currentWordBank }
                
                // 如果有单词不属于当前词库，则需要重新生成单词列表
                if (wordsNotInCurrentBank.isNotEmpty()) {
                    // 从当前词库中获取未完成的单词（按ID排序，获取最早添加的单词）
                    val newWords = repository.getUncompletedWordsByWordBankOrdered(currentWordBank, dailyGoal.targetWordCount)
                    currentDailyWordIds = newWords.map { it.id }
                    
                    // 更新dailyGoal中的单词ID列表
                    val updatedGoal = dailyGoal.copy(
                        dailyWordIds = currentDailyWordIds.joinToString(",")
                    )
                    repository.updateDailyGoal(updatedGoal)
                    dailyGoal = updatedGoal
                } else {
                    // 检查目标数量是否发生变化
                    if (currentDailyWordIds.size != dailyGoal.targetWordCount) {
                        if (currentDailyWordIds.size < dailyGoal.targetWordCount) {
                            // 需要补充单词，从当前词库中获取
                            val wordsToSupplementCount = dailyGoal.targetWordCount - currentDailyWordIds.size
                            val additionalWords = repository.getAdditionalUncompletedWordsByWordBank(currentWordBank, wordsToSupplementCount, currentDailyWordIds)
                            currentDailyWordIds = currentDailyWordIds + additionalWords.map { it.id }
                        } else {
                            // 需要删减单词
                            currentDailyWordIds = currentDailyWordIds.take(dailyGoal.targetWordCount)
                        }
                        // 更新DailyGoal中的dailyWordIds
                        val updatedGoal = dailyGoal.copy(
                            dailyWordIds = currentDailyWordIds.joinToString(","),
                            flippedWordIds = dailyGoal.flippedWordIds
                        )
                        repository.updateDailyGoal(updatedGoal)
                        dailyGoal = updatedGoal
                    }
                }
            }

            // 从数据库加载已翻转的单词ID
            val flippedWordIds = dailyGoal.flippedWordIds
                .split(",")
                .mapNotNull { it.toLongOrNull() }
                .toSet()
            _flippedWords.value = flippedWordIds

            val wordsForToday = if (currentDailyWordIds.isNotEmpty()) repository.getWordsByIds(currentDailyWordIds) else emptyList()
            
            val completedIds = repository.getCompletedWordIdsForDate(today)
            val wordsToShow = wordsForToday.filter { !completedIds.contains(it.id) }

            _todayWords.value = wordsToShow
            _isLoading.value = false
        }
    }
    
    /**
     * 更新当前学习位置
     * 
     * @param position 当前单词在列表中的位置
     */
    fun updateCurrentPosition(position: Int) {
        _currentPosition.value = position
    }
    
    /**
     * 切换单词的收藏状态
     * 
     * @param wordId 单词ID
     * @param isFavorite true表示添加到收藏，false表示从收藏中移除
     */
    fun toggleFavorite(wordId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                repository.insertFavoriteWord(com.justbyheart.vocabulary.data.entity.FavoriteWord(wordId = wordId))
            } else {
                repository.deleteFavoriteWordByWordId(wordId)
            }
        }
    }
    
    // 存储在本次学习中被翻转过的单词ID
    // val flippedWords = mutableSetOf<Long>()

    /**
     * 添加一个翻转过的单词ID到集合中
     * @param wordId 被翻转的单词ID
     */
    fun addFlippedWord(wordId: Long) {
        viewModelScope.launch {
            val today = getTodayZeroed()
            val dailyGoal = repository.getDailyGoalByDate(today)
            if (dailyGoal != null) {
                val currentFlippedWords = dailyGoal.flippedWordIds
                    .split(",")
                    .mapNotNull { it.toLongOrNull() }
                    .toMutableSet()
                
                // 只有当单词尚未在翻转列表中时才添加并更新数据库
                if (currentFlippedWords.add(wordId)) {
                    val updatedGoal = dailyGoal.copy(
                        flippedWordIds = currentFlippedWords.joinToString(",")
                    )
                    repository.updateDailyGoal(updatedGoal)
                    
                    // 更新LiveData
                    _flippedWords.value = currentFlippedWords
                }
            }
        }
    }

    /**
     * 从翻转过的单词ID集合中移除
     * @param wordId 被翻转回正面的单词ID
     */
    fun removeFlippedWord(wordId: Long) {
        viewModelScope.launch {
            val today = getTodayZeroed()
            val dailyGoal = repository.getDailyGoalByDate(today)
            if (dailyGoal != null) {
                val currentFlippedWords = dailyGoal.flippedWordIds
                    .split(",")
                    .mapNotNull { it.toLongOrNull() }
                    .toMutableSet()
                
                // 只有当单词在翻转列表中时才移除并更新数据库
                if (currentFlippedWords.remove(wordId)) {
                    val updatedGoal = dailyGoal.copy(
                        flippedWordIds = currentFlippedWords.joinToString(",")
                    )
                    repository.updateDailyGoal(updatedGoal)
                    
                    // 更新LiveData
                    _flippedWords.value = currentFlippedWords
                }
            }
        }
    }
    
    /**
     * 标记单词为已掌握
     * 
     * @param wordId 单词ID
     * @param isMemorized true表示标记为已掌握，false表示取消已掌握标记
     */
    fun markAsMemorized(wordId: Long, isMemorized: Boolean) {
        viewModelScope.launch {
            val today = getTodayZeroed()
            var studyRecord = repository.getStudyRecordByWordIdAndDate(wordId, today)
            
            if (studyRecord == null) {
                // 如果学习记录不存在，创建新的记录
                studyRecord = StudyRecord(
                    wordId = wordId,
                    studyDate = today,
                    isCompleted = isMemorized
                )
            } else {
                // 如果学习记录存在，更新完成状态
                studyRecord = studyRecord.copy(isCompleted = isMemorized)
            }
            
            repository.insertStudyRecord(studyRecord)
            
            // 如果是标记为已掌握，同时更新今日目标的完成计数
            if (isMemorized) {
                val dailyGoal = repository.getDailyGoalByDate(today)
                dailyGoal?.let {
                    val completedCount = repository.getCompletedWordsCountByDate(today)
                    repository.updateDailyGoalCompletedCount(today, completedCount)
                }
            }
        }
    }
    
    /**
     * 获取指定日期已完成的单词ID列表
     *
     * @param date 指定日期
     * @return 已完成的单词ID列表
     */
    suspend fun getCompletedWordIdsForDate(date: Date): List<Long> {
        return repository.getCompletedWordIdsForDate(date)
    }

    /**
     * 获取今天零点的时间戳
     *
     * @return 今天零点的时间戳
     */
    fun getTodayZeroed(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
    
    /**
     * 获取当前词库中的单词
     *
     * @param wordIds 单词ID列表
     * @return 属于当前词库的单词ID列表
     */
    suspend fun getWordsInCurrentBank(wordIds: List<Long>, context: Context): List<Long> {
        // 获取当前词库
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
        
        // 获取单词详情
        val words = repository.getWordsByIds(wordIds)
        
        // 筛选出属于当前词库的单词
        return words.filter { it.wordBank == currentWordBank }.map { it.id }
    }
    
    /**
     * 获取指定日期标记为已背的单词
     * 
     * @param date 指定日期
     * @return 已背的单词列表
     */
    suspend fun getMemorizedWordsByDate(date: Date): List<Word> {
        return repository.getMemorizedWordsByDate(date)
    }
}