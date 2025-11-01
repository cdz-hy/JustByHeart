package com.justbyheart.vocabulary.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.DailyGoal
import com.justbyheart.vocabulary.data.repository.WordRepository
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
    
    private val _overallProgress = MutableLiveData<Pair<Int, Int>>()
    val overallProgress: LiveData<Pair<Int, Int>> = _overallProgress
    
    private var todayWordIds: LongArray = longArrayOf()
    
    // SharedPreferences常量
    private companion object {
        const val PREFS_NAME = "vocabulary_settings"
        const val KEY_DAILY_WORD_COUNT = "daily_word_count"   // 每日单词数键名
        const val KEY_CURRENT_WORD_BANK = "current_word_bank"
        const val DEFAULT_WORD_BANK = "六级核心"
        const val DEFAULT_DAILY_WORD_COUNT = 10               // 默认每日单词数
    }

    /**
     * 加载今日学习进度
     * 
     * 从数据库获取今日的学习目标和完成情况，
     * 如果今日还没有目标记录，则创建一个默认目标（10个单词）。
     */
    fun loadTodayProgress(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // 获取当前词库
                val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
                
                // 获取用户设置的每日单词数
                val userSetDailyWordCount = sharedPreferences.getInt(KEY_DAILY_WORD_COUNT, DEFAULT_DAILY_WORD_COUNT)
                
                // 获取今日零点时间，用于查询今日目标
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                // 查询今日的学习目标
                val dailyGoal = repository.getDailyGoalByDate(today)
                val dailyWordIds = dailyGoal?.dailyWordIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
                
                // 保存今日单词ID供其他地方使用
                todayWordIds = dailyWordIds.toLongArray()

                val completedCount = if (dailyWordIds.isNotEmpty()) {
                    repository.getCompletedWordsCountForSpecificWords(dailyWordIds, today)
                } else {
                    0
                }
                
                if (dailyGoal == null) {
                    // 如果今日没有目标记录，创建默认目标（使用用户设置的单词数）
                    val newGoal = DailyGoal(date = today, targetWordCount = userSetDailyWordCount)
                    repository.insertDailyGoal(newGoal)
                    _dailyProgress.postValue(DailyProgress(target = userSetDailyWordCount, completed = completedCount))
                } else {
                    // 检查目标中的单词是否都属于当前词库
                    if (dailyWordIds.isNotEmpty()) {
                        val wordsInGoal = repository.getWordsByIds(dailyWordIds)
                        val wordsNotInCurrentBank = wordsInGoal.filter { it.wordBank != currentWordBank }
                        
                        // 如果有单词不属于当前词库，则需要重新生成单词列表
                        if (wordsNotInCurrentBank.isNotEmpty()) {
                            // 获取当前词库中未完成的单词（按ID排序，获取最早添加的单词）
                            val uncompletedWordsInCurrentBank = repository.getUncompletedWordsByWordBankOrdered(currentWordBank, dailyGoal.targetWordCount)
                            val newWordIds = uncompletedWordsInCurrentBank.map { it.id }
                            
                            // 更新dailyGoal中的单词ID列表
                            val updatedGoal = dailyGoal.copy(
                                dailyWordIds = newWordIds.joinToString(",")
                            )
                            repository.updateDailyGoal(updatedGoal)
                            
                            // 更新todayWordIds
                            todayWordIds = newWordIds.toLongArray()
                            
                            // 重新计算完成数量
                            val newCompletedCount = if (newWordIds.isNotEmpty()) {
                                repository.getCompletedWordsCountForSpecificWords(newWordIds, today)
                            } else {
                                0
                            }
                            
                            _dailyProgress.postValue(DailyProgress(
                                target = dailyGoal.targetWordCount,
                                completed = newCompletedCount
                            ))
                        } else {
                            // 使用已有的目标记录
                            _dailyProgress.postValue(DailyProgress(
                                target = dailyGoal.targetWordCount,
                                completed = completedCount
                            ))
                        }
                    } else {
                        // 使用已有的目标记录
                        _dailyProgress.postValue(DailyProgress(
                            target = dailyGoal.targetWordCount,
                            completed = completedCount
                        ))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun loadOverallProgress(context: Context) {
        viewModelScope.launch {
            try {
                // 获取当前词库
                val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
                
                val memorized = repository.getMemorizedWordsCountByWordBank(currentWordBank)
                val total = repository.getWordCountByWordBank(currentWordBank)
                _overallProgress.postValue(Pair(memorized, total))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getTodayWordIds(callback: (LongArray?) -> Unit) {
        callback(todayWordIds)
    }
}