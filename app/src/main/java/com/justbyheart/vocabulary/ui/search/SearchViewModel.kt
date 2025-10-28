package com.justbyheart.vocabulary.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch

/**
 * 搜索功能的ViewModel
 * 负责处理搜索逻辑和管理搜索结果数据
 */
class SearchViewModel(private val repository: WordRepository) : ViewModel() {

    // 搜索结果列表
    private val _searchResults = MutableLiveData<List<Word>>()
    val searchResults: LiveData<List<Word>> = _searchResults

    // 加载状态标识
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * 搜索单词方法
     * @param query 搜索关键词
     */
    fun searchWords(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // 如果查询为空，则清空结果列表
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                _isLoading.value = false
                return@launch
            }
            // 实现模糊搜索逻辑
            // 目前使用简单的包含检查
            val allWords = repository.getAllWordsList()
            val filteredWords = allWords.filter { word ->
                word.english.contains(query, ignoreCase = true) ||           // 英文匹配
                word.chinese.contains(query, ignoreCase = true) ||           // 中文匹配
                word.definition?.contains(query, ignoreCase = true) == true  // 定义匹配
            }
            _searchResults.value = filteredWords
            _isLoading.value = false
        }
    }
}