package com.ielts.vocabulary.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ielts.vocabulary.data.entity.Word
import com.ielts.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: WordRepository) : ViewModel() {
    
    val favoriteWords: LiveData<List<Word>> = repository.getFavoriteWords()
    
    fun removeFromFavorites(wordId: Long) {
        viewModelScope.launch {
            repository.removeFromFavorites(wordId)
        }
    }
}