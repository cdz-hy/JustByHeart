package com.justbyheart.vocabulary.ui.library

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

    fun loadUncompletedWords() {
        viewModelScope.launch {
            _uncompletedWords.value = repository.getUncompletedWords(Int.MAX_VALUE) // Get all uncompleted words
        }
    }

    fun loadCompletedWords() {
        viewModelScope.launch {
            _completedWords.value = repository.getCompletedWordsForDate(Date())
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