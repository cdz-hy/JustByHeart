package com.justbyheart.vocabulary.ui.favorites

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

class FavoritesViewModel(private val repository: WordRepository) : ViewModel() {
    
    private val _favoriteWords = MutableStateFlow<List<Word>>(emptyList())
    val favoriteWords = _favoriteWords.asStateFlow()
    
    // SharedPreferences常量
    private companion object {
        const val PREFS_NAME = "vocabulary_settings"
        const val KEY_CURRENT_WORD_BANK = "current_word_bank"
        const val DEFAULT_WORD_BANK = "六级核心"
    }
    
    fun loadFavoriteWords(context: Context) {
        viewModelScope.launch {
            try {
                // 获取当前词库
                val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
                
                // 获取当前词库中的收藏单词
                val favoriteWordsInCurrentBank = repository.getFavoriteWordsByWordBank(currentWordBank)
                _favoriteWords.value = favoriteWordsInCurrentBank
            } catch (e: Exception) {
                e.printStackTrace()
                _favoriteWords.value = emptyList()
            }
        }
    }
    
    fun removeFromFavorites(wordId: Long) {
        viewModelScope.launch {
            repository.deleteFavoriteWordByWordId(wordId)
        }
    }
}