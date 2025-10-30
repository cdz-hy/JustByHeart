package com.justbyheart.vocabulary.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.DailyGoal
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch
import java.util.*

class SettingsViewModel(private val repository: WordRepository) : ViewModel() {
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun updateDailyWordCount(count: Int) {
        viewModelScope.launch {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            val existingGoal = repository.getDailyGoalByDate(today)
            if (existingGoal != null) {
                val updatedGoal = existingGoal.copy(targetWordCount = count)
                repository.updateDailyGoal(updatedGoal)
            } else {
                val newGoal = DailyGoal(date = today, targetWordCount = count)
                repository.insertDailyGoal(newGoal)
            }
        }
    }
    
    fun initializeWords(words: List<Word>) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.insertWords(words)
            _isLoading.value = false
        }
    }
    
    suspend fun getWordCount(): Int {
        return repository.getTotalWordsCount()
    }
    
    suspend fun clearAllWords() {
        repository.deleteAllWords()
    }
}