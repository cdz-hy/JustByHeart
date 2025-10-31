package com.justbyheart.vocabulary.ui.library

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch
import java.util.*

class LibraryViewModel(private val repository: WordRepository) : ViewModel() {

    private val _uncompletedWords = MutableLiveData<List<Word>>()
    val uncompletedWords: LiveData<List<Word>> = _uncompletedWords

    private val _completedWords = MutableLiveData<List<Word>>()
    val completedWords: LiveData<List<Word>> = _completedWords
    
    // SharedPreferences常量
    private companion object {
        const val PREFS_NAME = "vocabulary_settings"
        const val KEY_CURRENT_WORD_BANK = "current_word_bank"
        const val DEFAULT_WORD_BANK = "六级核心"
    }

    fun loadUncompletedWords(context: Context) {
        viewModelScope.launch {
            // 获取当前词库
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
            
            _uncompletedWords.value = repository.getUncompletedWordsByWordBank(currentWordBank, Int.MAX_VALUE)
        }
    }

    fun loadCompletedWords(context: Context) {
        viewModelScope.launch {
            // 获取当前词库
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
            
            _completedWords.value = repository.getCompletedWordsByWordBank(currentWordBank)
        }
    }
}

class LibraryViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}