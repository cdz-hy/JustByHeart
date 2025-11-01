package com.justbyheart.vocabulary.ui.review

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class ReviewViewModel(private val repository: WordRepository) : ViewModel() {
    
    private val _reviewWords = MutableLiveData<List<Word>>()
    val reviewWords: LiveData<List<Word>> = _reviewWords
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // SharedPreferences常量
    private companion object {
        const val PREFS_NAME = "vocabulary_settings"
        const val KEY_CURRENT_WORD_BANK = "current_word_bank"
        const val DEFAULT_WORD_BANK = "六级核心"
    }
    
    fun getTodayZeroed(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun loadStudyDates() {
        // 加载历史学习日期
    }
    
    fun loadWordsForDate(date: Date, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 获取当前词库
                val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
                
                // 获取指定日期已完成的单词
                val allCompletedWords = repository.getCompletedWordsForDate(date)
                
                // 筛选出当前词库中的单词
                val completedWordsInCurrentBank = allCompletedWords.filter { it.wordBank == currentWordBank }
                
                _reviewWords.value = completedWordsInCurrentBank
            } catch (e: Exception) {
                e.printStackTrace()
                _reviewWords.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}