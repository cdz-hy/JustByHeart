package com.ielts.vocabulary.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ielts.vocabulary.data.entity.DailyGoal
import com.ielts.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * 每日学习进度数据类
 * 
 * @property target 目标单词数量
 * @property completed 已完成单词数量
 */
data class DailyProgress(
    val target: Int,        // 目标单词数
    val completed: Int      // 已完成单词数
)

/**
 * 主页ViewModel
 * 
 * 负责管理主页的业务逻辑和数据状态，包括：
 * - 加载和显示每日学习进度
 * - 管理每日学习目标
 * - 处理加载状态
 * 
 * @param repository 数据仓库，用于访问数据层
 */
class HomeViewModel(private val repository: WordRepository) : ViewModel() {
    
    // 每日学习进度的私有可变LiveData
    private val _dailyProgress = MutableLiveData<DailyProgress>()
    // 对外暴露的只读LiveData
    val dailyProgress: LiveData<DailyProgress> = _dailyProgress
    
    // 加载状态的私有可变LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    // 对外暴露的只读LiveData
    val isLoading: LiveData<Boolean> = _isLoading
    
    /**
     * 加载今日学习进度
     * 
     * 从数据库获取今日的学习目标和完成情况，
     * 如果今日还没有目标记录，则创建一个默认目标（10个单词）。
     */
    fun loadTodayProgress() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 获取今日零点时间，用于查询今日目标
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            // 查询今日的学习目标
            val dailyGoal = repository.getDailyGoalByDate(today)
            
            if (dailyGoal == null) {
                // 如果今日没有目标记录，创建默认目标
                val newGoal = DailyGoal(date = today, targetWordCount = 10)
                repository.insertDailyGoal(newGoal)
                _dailyProgress.value = DailyProgress(target = 10, completed = 0)
            } else {
                // 使用已有的目标记录
                _dailyProgress.value = DailyProgress(
                    target = dailyGoal.targetWordCount,
                    completed = dailyGoal.completedWordCount
                )
            }
            
            _isLoading.value = false
        }
    }
}