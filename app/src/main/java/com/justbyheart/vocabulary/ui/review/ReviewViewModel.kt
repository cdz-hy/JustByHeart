package com.justbyheart.vocabulary.ui.review

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
    
    fun loadWordsForDate(date: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            val words = repository.getCompletedWordsForDate(date)
            _reviewWords.value = words
            _isLoading.value = false
        }
    }
}