package com.justbyheart.vocabulary.ui.study

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    
    /**
     * 加载今日学习单词
     * 
     * 根据今日的学习目标数量，获取相应数量的单词，
     */
    fun loadTodayWords() {
        viewModelScope.launch {
            _isLoading.value = true
            val today = getTodayZeroed()

            var dailyGoal = repository.getDailyGoalByDate(today)
            var currentDailyWordIds: List<Long>

            // 如果今日目标不存在，或者每日单词ID列表为空，则生成新的今日单词列表并保存
            if (dailyGoal == null || dailyGoal.dailyWordIds.isEmpty()) {
                val targetCount = dailyGoal?.targetWordCount ?: 10
                val newWords = repository.getUncompletedWords(targetCount)
                currentDailyWordIds = newWords.map { it.id }
                
                val updatedGoal = (dailyGoal ?: DailyGoal(date = today)).copy(
                    dailyWordIds = currentDailyWordIds.joinToString(","),
                    targetWordCount = targetCount,
                    flippedWordIds = dailyGoal?.flippedWordIds ?: ""
                )
                repository.insertDailyGoal(updatedGoal)
                dailyGoal = updatedGoal
            } else {
                // 如果今日目标存在且每日单词ID列表不为空，则使用存储的ID列表
                currentDailyWordIds = dailyGoal.dailyWordIds.split(",").map { it.toLong() }

                // 检查目标数量是否发生变化
                val newTargetCount = dailyGoal.targetWordCount
                if (currentDailyWordIds.size != newTargetCount) {
                    if (currentDailyWordIds.size < newTargetCount) {
                        // 需要补充单词
                        val wordsToSupplementCount = newTargetCount - currentDailyWordIds.size
                        val additionalWords = repository.getAdditionalUncompletedWords(wordsToSupplementCount, currentDailyWordIds)
                        currentDailyWordIds = currentDailyWordIds + additionalWords.map { it.id }
                    } else {
                        // 需要删减单词
                        currentDailyWordIds = currentDailyWordIds.take(newTargetCount)
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
    
    private fun getTodayZeroed(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
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
                repository.addToFavorites(wordId)
            } else {
                repository.removeFromFavorites(wordId)
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
     * 标记今日学习为完成状态
     * 
     * 更新今日学习目标的完成情况，
     * 将已完成单词数设置为实际学习的单词数。
     */
    fun markTodayComplete() {
        viewModelScope.launch {
            // 获取今日零点时间
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            // 更新今日学习目标的完成状态
            val dailyGoal = repository.getDailyGoalByDate(today)
            dailyGoal?.let { goal ->
                val updatedGoal = goal.copy(
                    completedWordCount = _todayWords.value?.size ?: 0,
                    isCompleted = true
                )
                repository.updateDailyGoal(updatedGoal)
            }
        }
    }

    /**
     * 获取今日已完成的单词ID列表
     * @return 已完成的单词ID列表
     */
    suspend fun getCompletedWordIdsForToday(): List<Long> {
        val today = getTodayZeroed()
        return repository.getCompletedWordIdsForDate(today)
    }
}