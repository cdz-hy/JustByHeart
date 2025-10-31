package com.justbyheart.vocabulary.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.DailyGoal
import com.justbyheart.vocabulary.data.entity.FavoriteWord
import com.justbyheart.vocabulary.data.entity.StudyRecord
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch
import java.util.*

class SettingsViewModel(private val repository: WordRepository) : ViewModel() {
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun updateDailyWordCount(count: Int) {
        viewModelScope.launch {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            val existingGoal = repository.getDailyGoalByDate(today)
            if (existingGoal != null) {
                val updatedGoal = existingGoal.copy(targetWordCount = count)
                repository.updateDailyGoal(updatedGoal)
            } else {
                val newGoal = DailyGoal(date = today, targetWordCount = count)
                repository.insertDailyGoal(newGoal)
            }
        }
    }
    
    fun initializeWords(words: List<Word>) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.insertWords(words)
            _isLoading.value = false
        }
    }
    
    suspend fun getWordCount(): Int {
        return repository.getWordCount()
    }
    
    suspend fun getWordCountByWordBank(wordBank: String): Int {
        return repository.getWordCountByWordBank(wordBank)
    }
    
    suspend fun clearAllWords() {
        repository.deleteAllWords()
    }
    
    fun migrateStudyRecords(fromWordBank: String, toWordBank: String) {
        viewModelScope.launch {
            repository.migrateStudyRecords(fromWordBank, toWordBank)
        }
    }
    
    fun migrateFavoriteWords(fromWordBank: String, toWordBank: String) {
        viewModelScope.launch {
            repository.migrateFavoriteWords(fromWordBank, toWordBank)
        }
    }
    
    // 为导入导出功能添加的方法
    
    suspend fun getCompletedWordsWithRecordsByWordBank(wordBank: String): List<Pair<Word, StudyRecord>> {
        // 获取指定词库中的所有单词
        val wordsInBank = repository.getWordsByWordBank(wordBank)
        val wordIds = wordsInBank.map { it.id }
        
        if (wordIds.isEmpty()) return emptyList()
        
        // 获取这些单词的所有学习记录
        val allStudyRecords = mutableMapOf<Long, StudyRecord>()
        for (wordId in wordIds) {
            val records = repository.getStudyRecordsByWordId(wordId)
            // 只取已完成(isCompleted = true)的记录
            val completedRecords = records.filter { it.isCompleted }
            // 如果有多个已完成记录，取最新的一个
            completedRecords.maxByOrNull { it.studyDate }?.let { latestRecord ->
                allStudyRecords[wordId] = latestRecord
            }
        }
        
        // 构建单词和学习记录的配对列表
        return wordsInBank.mapNotNull { word ->
            allStudyRecords[word.id]?.let { record ->
                word to record
            }
        }
    }
    
    suspend fun getFavoriteWordsByWordBank(wordBank: String): List<Word> {
        return repository.getFavoriteWordsByWordBank(wordBank)
    }
    
    suspend fun getWordByEnglishAndWordBank(english: String, wordBank: String): Word? {
        return repository.getWordByEnglishAndWordBank(english, wordBank)
    }
    
    suspend fun isWordFavorite(wordId: Long): Boolean {
        return repository.isWordFavorite(wordId)
    }
    
    suspend fun insertFavoriteWord(wordId: Long) {
        val favoriteWord = FavoriteWord(wordId = wordId)
        repository.insertFavoriteWord(favoriteWord)
    }
    
    suspend fun getStudyRecordByWordIdAndDate(wordId: Long, date: Date): StudyRecord? {
        return repository.getStudyRecordByWordIdAndDate(wordId, date)
    }
    
    suspend fun updateStudyRecord(studyRecord: StudyRecord) {
        repository.updateStudyRecord(studyRecord)
    }
    
    suspend fun insertStudyRecord(studyRecord: StudyRecord) {
        repository.insertStudyRecord(studyRecord)
    }
    
    suspend fun getStudyRecordsByWordId(wordId: Long): List<StudyRecord> {
        return repository.getStudyRecordsByWordId(wordId)
    }
}