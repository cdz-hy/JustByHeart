package com.justbyheart.vocabulary.ui.worddisplay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch

class WordDisplayViewModel(private val repository: WordRepository) : ViewModel() {

    private val _word = MutableLiveData<Word?>()
    val word: LiveData<Word?> = _word

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    fun loadWord(wordId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val loadedWord = repository.getWordById(wordId)
            _word.value = loadedWord
            loadFavoriteStatus(wordId)
            _isLoading.value = false
        }
    }

    private fun loadFavoriteStatus(wordId: Long) {
        viewModelScope.launch {
            _isFavorite.value = repository.isFavorite(wordId)
        }
    }

    fun toggleFavorite() {
        val wordId = _word.value?.id ?: return
        val currentStatus = _isFavorite.value ?: false
        viewModelScope.launch {
            if (currentStatus) {
                repository.removeFromFavorites(wordId)
            } else {
                repository.addToFavorites(wordId)
            }
            _isFavorite.value = !currentStatus
        }
    }
}