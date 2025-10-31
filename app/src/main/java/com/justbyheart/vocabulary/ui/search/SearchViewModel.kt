package com.justbyheart.vocabulary.ui.search

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch

/**
 * 搜索功能ViewModel
 * 负责处理搜索逻辑和管理搜索结果
 */
class SearchViewModel(private val repository: WordRepository) : ViewModel() {

    // 搜索结果的私有可变LiveData
    private val _searchResults = MutableLiveData<List<Word>>()
    // 对外暴露的只读LiveData
    val searchResults: LiveData<List<Word>> = _searchResults

    // 加载状态的私有可变LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    // 对外暴露的只读LiveData
    val isLoading: LiveData<Boolean> = _isLoading
    
    // SharedPreferences常量
    private companion object {
        const val PREFS_NAME = "vocabulary_settings"
        const val KEY_CURRENT_WORD_BANK = "current_word_bank"
        const val DEFAULT_WORD_BANK = "六级核心"
    }

    /**
     * 搜索单词
     * @param query 搜索关键词
     */
    fun searchWords(query: String, context: Context) {
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            // 获取当前词库
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
            
            // 在当前词库中搜索匹配的单词
            val results = repository.searchWordsInWordBank(query, currentWordBank)
            _searchResults.value = results
            _isLoading.value = false
        }
    }
}