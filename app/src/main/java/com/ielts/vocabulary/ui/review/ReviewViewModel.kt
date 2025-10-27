package com.ielts.vocabulary.ui.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ielts.vocabulary.data.entity.Word
import com.ielts.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch
import java.util.Date

class ReviewViewModel(private val repository: WordRepository) : ViewModel() {
    
    private val _reviewWords = MutableLiveData<List<Word>>()
    val reviewWords: LiveData<List<Word>> = _reviewWords
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadStudyDates() {
        // 加载历史学习日期
    }
    
    fun loadWordsForDate(date: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 这里应该根据日期加载对应的单词
            // 为了简化，我们暂时返回随机单词
            val words = repository.getRandomWords(5)
            _reviewWords.value = words
            
            _isLoading.value = false
        }
    }
}