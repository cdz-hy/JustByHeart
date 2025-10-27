package com.ielts.vocabulary.ui.study

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ielts.vocabulary.data.entity.StudyRecord
import com.ielts.vocabulary.data.entity.Word
import com.ielts.vocabulary.data.repository.WordRepository
import com.ielts.vocabulary.utils.WordDataLoader
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
    
    /**
     * 加载今日学习单词
     * 
     * 根据今日的学习目标数量，随机获取相应数量的单词，
     * 并为每个单词创建学习记录。
     */
    fun loadTodayWords() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 获取今日零点时间
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            // 获取今日学习目标，如果没有则默认10个单词
            val dailyGoal = repository.getDailyGoalByDate(today)
            val targetCount = dailyGoal?.targetWordCount ?: 10
            
            // 随机获取指定数量的单词
            val words = repository.getRandomWords(targetCount)
            _todayWords.value = words
            
            // 为每个单词创建学习记录
            words.forEach { word ->
                val studyRecord = StudyRecord(
                    wordId = word.id,
                    studyDate = today,
                    isCompleted = false
                )
                repository.insertStudyRecord(studyRecord)
            }
            
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
                repository.addToFavorites(wordId)
            } else {
                repository.removeFromFavorites(wordId)
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
}