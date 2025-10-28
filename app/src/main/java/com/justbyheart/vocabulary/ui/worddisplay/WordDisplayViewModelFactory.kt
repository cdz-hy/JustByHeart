package com.justbyheart.vocabulary.ui.worddisplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.justbyheart.vocabulary.data.repository.WordRepository

class WordDisplayViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordDisplayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordDisplayViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}