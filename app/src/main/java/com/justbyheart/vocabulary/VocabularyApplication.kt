package com.justbyheart.vocabulary

import android.app.Application
import androidx.lifecycle.lifecycleScope
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.utils.WordDataLoader
import kotlinx.coroutines.launch

class VocabularyApplication : Application() {
    
    val database by lazy { VocabularyDatabase.getDatabase(this) }
    val repository by lazy {
        WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化单词数据（仅在首次启动时）
        initializeWordsIfNeeded()
    }
    
    private fun initializeWordsIfNeeded() {
        // 这里可以添加检查逻辑，判断是否需要初始化数据
        // 为了简化，我们暂时跳过自动初始化，让用户在设置中手动初始化
    }
}