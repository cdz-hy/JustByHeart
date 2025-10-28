package com.justbyheart.vocabulary.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: WordRepository) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Word>>()
    val searchResults: LiveData<List<Word>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun searchWords(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                _isLoading.value = false
                return@launch
            }
            // Implement fuzzy search logic here
            // For now, a simple contains check
            val allWords = repository.getAllWordsList()
            val filteredWords = allWords.filter { word ->
                word.english.contains(query, ignoreCase = true) ||
                word.chinese.contains(query, ignoreCase = true) ||
                word.definition?.contains(query, ignoreCase = true) == true
            }
            _searchResults.value = filteredWords
            _isLoading.value = false
        }
    }
}