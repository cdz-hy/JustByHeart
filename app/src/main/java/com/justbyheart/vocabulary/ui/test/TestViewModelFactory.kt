package com.justbyheart.vocabulary.ui.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.justbyheart.vocabulary.data.repository.WordRepository

class TestViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}