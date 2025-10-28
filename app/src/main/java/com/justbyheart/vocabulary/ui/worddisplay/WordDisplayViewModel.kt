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

    fun loadWord(wordId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val loadedWord = repository.getWordById(wordId)
            _word.value = loadedWord
            _isLoading.value = false
        }
    }
}